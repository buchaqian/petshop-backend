package com.petshop.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.entity.Commission;
import com.petshop.entity.DistributorApply;
import com.petshop.entity.User;
import com.petshop.mapper.CommissionMapper;
import com.petshop.mapper.DistributorApplyMapper;
import com.petshop.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户端-分销")
@RestController
@RequestMapping("/app/distribution")
@RequiredArgsConstructor
public class AppDistributionController {

    private final DistributorApplyMapper applyMapper;
    private final CommissionMapper commissionMapper;
    private final UserMapper userMapper;

    @Operation(summary = "申请成为分销商")
    @PostMapping("/apply")
    public Result<Void> apply(@RequestParam(required = false) String reason) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 检查是否已申请
        DistributorApply existing = applyMapper.selectOne(new LambdaQueryWrapper<DistributorApply>()
                .eq(DistributorApply::getUserId, userId)
                .eq(DistributorApply::getStatus, 0));
        if (existing != null) throw new RuntimeException("您已提交申请，请等待审核");

        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() >= 1) throw new RuntimeException("您已是分销商");

        DistributorApply apply = new DistributorApply();
        apply.setUserId(userId);
        apply.setStatus(0);
        apply.setReason(reason);
        applyMapper.insert(apply);
        return Result.success();
    }

    @Operation(summary = "我的申请状态")
    @GetMapping("/apply/status")
    public Result<DistributorApply> applyStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        DistributorApply apply = applyMapper.selectOne(new LambdaQueryWrapper<DistributorApply>()
                .eq(DistributorApply::getUserId, userId)
                .orderByDesc(DistributorApply::getCreateTime)
                .last("LIMIT 1"));
        return Result.success(apply);
    }

    @Operation(summary = "我的佣金记录")
    @GetMapping("/commission/list")
    public Result<List<Commission>> commissionList() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Commission> list = commissionMapper.selectList(new LambdaQueryWrapper<Commission>()
                .eq(Commission::getUserId, userId)
                .orderByDesc(Commission::getCreateTime));
        return Result.success(list);
    }

    @Operation(summary = "我的佣金统计")
    @GetMapping("/commission/stats")
    public Result<Map<String, Object>> commissionStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Commission> list = commissionMapper.selectList(new LambdaQueryWrapper<Commission>()
                .eq(Commission::getUserId, userId));

        BigDecimal total = list.stream().map(Commission::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settled = list.stream().filter(c -> c.getStatus() == 1)
                .map(Commission::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pending = total.subtract(settled);

        User user = userMapper.selectById(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCommission", total);
        stats.put("settledCommission", settled);
        stats.put("pendingCommission", pending);
        stats.put("balance", user != null ? user.getBalance() : BigDecimal.ZERO);
        return Result.success(stats);
    }
}
