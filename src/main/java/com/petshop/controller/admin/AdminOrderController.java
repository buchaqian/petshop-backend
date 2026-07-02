package com.petshop.controller.admin;

import com.petshop.common.PageResult;
import com.petshop.common.Result;
import com.petshop.service.OrderService;
import com.petshop.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-订单管理")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.adminGetOrderList(status, orderNo, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.adminGetOrderDetail(id));
    }

    @Operation(summary = "发货")
    @PostMapping("/deliver")
    public Result<Void> deliver(
            @RequestParam Long id,
            @RequestParam String trackingCompany,
            @RequestParam String trackingNo) {
        orderService.deliver(id, trackingCompany, trackingNo);
        return Result.success();
    }
}
