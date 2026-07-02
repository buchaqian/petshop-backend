package com.petshop.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.entity.Address;
import com.petshop.mapper.AddressMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-收货地址")
@RestController
@RequestMapping("/app/address")
@RequiredArgsConstructor
public class AppAddressController {

    private final AddressMapper addressMapper;

    @Operation(summary = "获取地址列表")
    @GetMapping("/list")
    public Result<List<Address>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)));
    }

    @Operation(summary = "新增地址")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        address.setUserId(userId);
        // 如果设为默认，先清除其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressMapper.insert(address);
        return Result.success();
    }

    @Operation(summary = "编辑地址")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        Address existing = addressMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressMapper.updateById(address);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        addressMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        clearDefault(userId);
        Address address = new Address();
        address.setId(id);
        address.setIsDefault(1);
        addressMapper.updateById(address);
        return Result.success();
    }

    private void clearDefault(Long userId) {
        Address clear = new Address();
        clear.setIsDefault(0);
        addressMapper.update(clear, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }
}
