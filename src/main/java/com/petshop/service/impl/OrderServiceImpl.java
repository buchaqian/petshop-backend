package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.common.PageResult;
import com.petshop.dto.CreateOrderDTO;
import com.petshop.entity.*;
import com.petshop.mapper.*;
import com.petshop.service.OrderService;
import com.petshop.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(getStatusText(order.getStatus()));

        User user = userMapper.selectById(order.getUserId());
        if (user != null) vo.setUserNickname(user.getNickname());

        Address address = addressMapper.selectById(order.getAddressId());
        if (address != null) {
            vo.setReceiverName(address.getName());
            vo.setReceiverPhone(address.getPhone());
            vo.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(item -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知";
        };
    }

    // ========== 管理端 ==========

    @Override
    public PageResult<OrderVO> adminGetOrderList(Integer status, String orderNo, Integer page, Integer size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(status != null, Order::getStatus, status)
                .eq(StringUtils.hasText(orderNo), Order::getOrderNo, orderNo)
                .orderByDesc(Order::getCreateTime);

        Page<Order> pageResult = this.page(new Page<>(page, size), wrapper);
        List<OrderVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList);
    }

    @Override
    public OrderVO adminGetOrderDetail(Long id) {
        Order order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        return toVO(order);
    }

    @Override
    public void deliver(Long id, String trackingCompany, String trackingNo) {
        Order order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("订单状态不正确，无法发货");
        order.setStatus(2);
        order.setTrackingCompany(trackingCompany);
        order.setTrackingNo(trackingNo);
        order.setDeliverTime(LocalDateTime.now());
        updateById(order);
    }

    // ========== 用户端 ==========

    @Override
    @Transactional
    public String createOrder(Long userId, CreateOrderDTO dto) {
        String orderNo = "ORD" + System.currentTimeMillis() + userId;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new RuntimeException("商品 " + itemDTO.getProductId() + " 不存在或已下架");
            }

            BigDecimal price = product.getPrice();
            String specName = null;

            if (itemDTO.getSpecId() != null) {
                ProductSpec spec = productSpecMapper.selectById(itemDTO.getSpecId());
                if (spec != null) {
                    price = spec.getPrice();
                    specName = spec.getSpecName();
                }
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setSpecName(specName);
            item.setPrice(price);
            item.setQuantity(itemDTO.getQuantity());
            item.setTotalAmount(itemTotal);
            orderItems.add(item);

            // 扣减库存
            product.setStock(product.getStock() - itemDTO.getQuantity());
            product.setSales(product.getSales() + itemDTO.getQuantity());
            productMapper.updateById(product);
        }

        // 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(0);
        order.setRemark(dto.getRemark());
        order.setFromDistributorId(dto.getFromDistributorId());
        save(order);

        // 保存订单明细
        orderItems.forEach(item -> {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        });

        return orderNo;
    }

    @Override
    public PageResult<OrderVO> getUserOrderList(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);

        Page<Order> pageResult = this.page(new Page<>(page, size), wrapper);
        List<OrderVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList);
    }

    @Override
    public OrderVO getUserOrderDetail(Long orderId, Long userId) {
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) throw new RuntimeException("订单不存在");
        return toVO(order);
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 0) throw new RuntimeException("只有待付款的订单可以取消");
        order.setStatus(4);
        updateById(order);
    }

    @Override
    public void confirmReceive(Long orderId, Long userId) {
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 2) throw new RuntimeException("只有待收货的订单可以确认收货");
        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        updateById(order);
    }
}
