package com.petshop.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.entity.Commission;
import com.petshop.entity.Order;
import com.petshop.entity.Product;
import com.petshop.entity.User;
import com.petshop.mapper.CommissionMapper;
import com.petshop.mapper.OrderMapper;
import com.petshop.mapper.ProductMapper;
import com.petshop.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理端-数据统计")
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CommissionMapper commissionMapper;

    @Operation(summary = "数据概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();

        List<Order> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().ne(Order::getStatus, 4));

        // 今日数据
        List<Order> todayOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null
                        && !o.getCreateTime().isBefore(todayStart)
                        && !o.getCreateTime().isAfter(todayEnd))
                .collect(Collectors.toList());
        BigDecimal todaySales = todayOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月数据
        List<Order> monthOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null
                        && !o.getCreateTime().isBefore(monthStartTime))
                .collect(Collectors.toList());
        BigDecimal monthSales = monthOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月佣金
        List<Commission> monthCommissions = commissionMapper.selectList(
                new LambdaQueryWrapper<Commission>()
                        .ge(Commission::getCreateTime, monthStartTime));
        BigDecimal monthCommission = monthCommissions.stream()
                .map(Commission::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月新增用户
        Long monthNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, monthStartTime));

        // 在售商品数
        Long onSaleProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));

        // 待发货订单数
        Long pendingShipOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todaySales", todaySales);
        result.put("todayOrders", todayOrders.size());
        result.put("monthSales", monthSales);
        result.put("monthOrders", monthOrders.size());
        result.put("monthCommission", monthCommission);
        result.put("monthNewUsers", monthNewUsers);
        result.put("onSaleProducts", onSaleProducts);
        result.put("pendingShipOrders", pendingShipOrders);
        return Result.success(result);
    }

    @Operation(summary = "近7日销售趋势")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.minusDays(6).atStartOfDay();

        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ne(Order::getStatus, 4)
                        .ge(Order::getCreateTime, startTime));

        Map<LocalDate, BigDecimal> salesByDay = orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getPayAmount() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getPayAmount, BigDecimal::add)));

        Map<LocalDate, Long> ordersByDay = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("sales", salesByDay.getOrDefault(date, BigDecimal.ZERO));
            item.put("orders", ordersByDay.getOrDefault(date, 0L));
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "商品销量排行")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking() {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT 10"));

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("sales", p.getSales());
            item.put("price", p.getPrice());
            item.put("mainImage", p.getMainImage());
            return item;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
