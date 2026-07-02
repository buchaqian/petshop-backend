package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.common.PageResult;
import com.petshop.entity.Order;
import com.petshop.vo.OrderVO;

public interface OrderService extends IService<Order> {

    // 管理端
    PageResult<OrderVO> adminGetOrderList(Integer status, String orderNo, Integer page, Integer size);
    OrderVO adminGetOrderDetail(Long id);
    void deliver(Long id, String trackingCompany, String trackingNo);

    // 用户端
    String createOrder(Long userId, com.petshop.dto.CreateOrderDTO dto);
    PageResult<OrderVO> getUserOrderList(Long userId, Integer status, Integer page, Integer size);
    OrderVO getUserOrderDetail(Long orderId, Long userId);
    void cancelOrder(Long orderId, Long userId);
    void confirmReceive(Long orderId, Long userId);
}
