package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.common.PageResult;
import com.petshop.dto.CreateOrderDTO;
import com.petshop.entity.Address;
import com.petshop.entity.Order;
import com.petshop.entity.OrderItem;
import com.petshop.entity.Product;
import com.petshop.entity.ProductSpec;
import com.petshop.entity.User;
import com.petshop.mapper.AddressMapper;
import com.petshop.mapper.OrderItemMapper;
import com.petshop.mapper.OrderMapper;
import com.petshop.mapper.ProductMapper;
import com.petshop.mapper.ProductSpecMapper;
import com.petshop.mapper.UserMapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final int STATUS_PENDING_PAYMENT = 0;
    private static final int STATUS_PENDING_DELIVERY = 1;
    private static final int STATUS_PENDING_RECEIVE = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_CANCELLED = 4;
    private static final int PRODUCT_ON_SALE = 1;

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
        if (user != null) {
            vo.setUserNickname(user.getNickname());
        }

        Address address = addressMapper.selectById(order.getAddressId());
        if (address != null) {
            vo.setReceiverName(address.getName());
            vo.setReceiverPhone(address.getPhone());
            vo.setReceiverAddress(formatAddress(address));
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
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case STATUS_PENDING_PAYMENT -> "Pending payment";
            case STATUS_PENDING_DELIVERY -> "Pending delivery";
            case STATUS_PENDING_RECEIVE -> "Pending receipt";
            case STATUS_FINISHED -> "Finished";
            case STATUS_CANCELLED -> "Cancelled";
            default -> "Unknown";
        };
    }

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
        if (order == null) {
            throw new IllegalArgumentException("Order does not exist");
        }
        return toVO(order);
    }

    @Override
    public void deliver(Long id, String trackingCompany, String trackingNo) {
        Order order = getById(id);
        if (order == null) {
            throw new IllegalArgumentException("Order does not exist");
        }
        if (!Integer.valueOf(STATUS_PENDING_DELIVERY).equals(order.getStatus())) {
            throw new IllegalArgumentException("Only pending-delivery orders can be delivered");
        }

        order.setStatus(STATUS_PENDING_RECEIVE);
        order.setTrackingCompany(trackingCompany);
        order.setTrackingNo(trackingNo);
        order.setDeliverTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public String createOrder(Long userId, CreateOrderDTO dto) {
        Address address = requireOwnedAddress(userId, dto.getAddressId());
        String orderNo = "ORD" + System.currentTimeMillis() + userId;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            int quantity = requirePositiveQuantity(itemDTO.getQuantity());
            Product product = requireAvailableProduct(itemDTO.getProductId());
            ProductSpec spec = itemDTO.getSpecId() == null
                    ? null
                    : requireProductSpec(product.getId(), itemDTO.getSpecId());

            BigDecimal price = resolvePrice(product, spec);
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotal);

            orderItems.add(createOrderItem(product, spec, price, quantity, itemTotal));

            // Use conditional updates so concurrent requests cannot reserve more stock than available.
            decrementInventory(product.getId(), spec == null ? null : spec.getId(), quantity);
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(STATUS_PENDING_PAYMENT);
        order.setRemark(dto.getRemark());
        order.setFromDistributorId(dto.getFromDistributorId());
        save(order);

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
        Order order = getOwnedOrder(orderId, userId);
        return toVO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId);
        if (!Integer.valueOf(STATUS_PENDING_PAYMENT).equals(order.getStatus())) {
            throw new IllegalArgumentException("Only pending-payment orders can be cancelled");
        }

        order.setStatus(STATUS_CANCELLED);
        updateById(order);

        // In the current flow, pending-payment orders reserve stock. Cancelling releases it.
        restoreInventory(order.getId());
    }

    @Override
    public void confirmReceive(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId);
        if (!Integer.valueOf(STATUS_PENDING_RECEIVE).equals(order.getStatus())) {
            throw new IllegalArgumentException("Only pending-receipt orders can be confirmed");
        }

        order.setStatus(STATUS_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        updateById(order);
    }

    private Address requireOwnedAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId));
        if (address == null) {
            throw new IllegalArgumentException("Address does not exist");
        }
        return address;
    }

    private Order getOwnedOrder(Long orderId, Long userId) {
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new IllegalArgumentException("Order does not exist");
        }
        return order;
    }

    private Product requireAvailableProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !Integer.valueOf(PRODUCT_ON_SALE).equals(product.getStatus())) {
            throw new IllegalArgumentException("Product does not exist or is not available");
        }
        return product;
    }

    private ProductSpec requireProductSpec(Long productId, Long specId) {
        ProductSpec spec = productSpecMapper.selectById(specId);
        if (spec == null || !Objects.equals(productId, spec.getProductId())) {
            throw new IllegalArgumentException("Product spec does not exist");
        }
        return spec;
    }

    private int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        return quantity;
    }

    private BigDecimal resolvePrice(Product product, ProductSpec spec) {
        BigDecimal price = spec == null ? product.getPrice() : spec.getPrice();
        if (price == null) {
            throw new IllegalArgumentException("Product price is not configured");
        }
        return price;
    }

    private OrderItem createOrderItem(Product product, ProductSpec spec, BigDecimal price, int quantity, BigDecimal totalAmount) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setSpecId(spec == null ? null : spec.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setSpecName(spec == null ? null : spec.getSpecName());
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setTotalAmount(totalAmount);
        return item;
    }

    private void decrementInventory(Long productId, Long specId, int quantity) {
        if (specId != null) {
            int specUpdated = productSpecMapper.update(null, new LambdaUpdateWrapper<ProductSpec>()
                    .eq(ProductSpec::getId, specId)
                    .eq(ProductSpec::getProductId, productId)
                    .ge(ProductSpec::getStock, quantity)
                    .setSql("stock = stock - " + quantity));
            if (specUpdated == 0) {
                throw new IllegalArgumentException("Insufficient spec stock");
            }
        }

        int productUpdated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getStatus, PRODUCT_ON_SALE)
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .setSql("sales = sales + " + quantity));
        if (productUpdated == 0) {
            throw new IllegalArgumentException("Insufficient product stock");
        }
    }

    private void restoreInventory(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            int quantity = requirePositiveQuantity(item.getQuantity());
            if (item.getSpecId() != null) {
                productSpecMapper.update(null, new LambdaUpdateWrapper<ProductSpec>()
                        .eq(ProductSpec::getId, item.getSpecId())
                        .setSql("stock = stock + " + quantity));
            }

            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + quantity)
                    .setSql("sales = GREATEST(sales - " + quantity + ", 0)"));
        }
    }

    private String formatAddress(Address address) {
        return safe(address.getProvince()) + safe(address.getCity())
                + safe(address.getDistrict()) + safe(address.getDetail());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
