package com.petshop.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.petshop.common.PageResult;
import com.petshop.common.Result;
import com.petshop.dto.CreateOrderDTO;
import com.petshop.service.OrderService;
import com.petshop.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-订单")
@RestController
@RequestMapping("/app/order")
@RequiredArgsConstructor
public class AppOrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody CreateOrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(orderService.createOrder(userId, dto));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(orderService.getUserOrderList(userId, status, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(orderService.getUserOrderDetail(id, userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(id, userId);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.confirmReceive(id, userId);
        return Result.success();
    }
}
