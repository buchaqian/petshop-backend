package com.petshop.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petshop.common.PageResult;
import com.petshop.common.Result;
import com.petshop.entity.DistributorApply;
import com.petshop.entity.User;
import com.petshop.mapper.DistributorApplyMapper;
import com.petshop.mapper.UserMapper;
import com.petshop.vo.DistributorApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "管理端-分销商管理")
@RestController
@RequestMapping("/admin/distributor")
@RequiredArgsConstructor
public class AdminDistributorController {

    private final DistributorApplyMapper applyMapper;
    private final UserMapper userMapper;

    @Operation(summary = "分销申请列表")
    @GetMapping("/apply/list")
    public Result<PageResult<DistributorApplyVO>> applyList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        LambdaQueryWrapper<DistributorApply> wrapper = new LambdaQueryWrapper<DistributorApply>()
                .eq(status != null, DistributorApply::getStatus, status)
                .orderByDesc(DistributorApply::getCreateTime);

        Page<DistributorApply> pageResult = applyMapper.selectPage(new Page<>(page, size), wrapper);
        List<DistributorApplyVO> voList = pageResult.getRecords().stream().map(apply -> {
            DistributorApplyVO vo = new DistributorApplyVO();
            vo.setId(apply.getId());
            vo.setUserId(apply.getUserId());
            vo.setStatus(apply.getStatus());
            vo.setStatusText(getStatusText(apply.getStatus()));
            vo.setReason(apply.getReason());
            vo.setRejectReason(apply.getRejectReason());
            vo.setCreateTime(apply.getCreateTime());
            vo.setAuditTime(apply.getUpdateTime());

            User user = userMapper.selectById(apply.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(PageResult.of(pageResult.getTotal(), voList));
    }

    @Operation(summary = "审核分销申请（通过）")
    @PostMapping("/apply/approve")
    public Result<Void> approve(@RequestParam Long id) {
        DistributorApply apply = applyMapper.selectById(id);
        if (apply == null) throw new RuntimeException("申请记录不存在");
        if (apply.getStatus() != 0) throw new RuntimeException("该申请已处理");

        apply.setStatus(1);
        applyMapper.updateById(apply);

        // 升级用户为一级分销商
        User user = userMapper.selectById(apply.getUserId());
        if (user != null) {
            user.setRole(1);
            user.setDistributorLevel(1);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @Operation(summary = "审核分销申请（拒绝）")
    @PostMapping("/apply/reject")
    public Result<Void> reject(@RequestParam Long id, @RequestParam String rejectReason) {
        DistributorApply apply = applyMapper.selectById(id);
        if (apply == null) throw new RuntimeException("申请记录不存在");
        if (apply.getStatus() != 0) throw new RuntimeException("该申请已处理");

        apply.setStatus(2);
        apply.setRejectReason(rejectReason);
        applyMapper.updateById(apply);
        return Result.success();
    }

    @Operation(summary = "分销商列表")
    @GetMapping("/list")
    public Result<PageResult<User>> distributorList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .in(User::getRole, 1, 2)
                .orderByDesc(User::getCreateTime);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getRecords()));
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}
