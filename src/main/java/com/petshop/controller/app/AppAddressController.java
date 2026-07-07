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

@Tag(name = "App address APIs")
@RestController
@RequestMapping("/app/address")
@RequiredArgsConstructor
public class AppAddressController {

    private final AddressMapper addressMapper;

    @Operation(summary = "List addresses")
    @GetMapping("/list")
    public Result<List<Address>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)));
    }

    @Operation(summary = "Add address")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (address == null) {
            throw new IllegalArgumentException("Address body is required");
        }

        address.setId(null);
        address.setUserId(userId);
        if (isDefault(address)) {
            clearDefault(userId);
        }

        addressMapper.insert(address);
        return Result.success();
    }

    @Operation(summary = "Update address")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (address == null || address.getId() == null) {
            throw new IllegalArgumentException("Address id is required");
        }

        Address existing = requireOwnedAddress(address.getId(), userId);
        address.setUserId(userId);
        if (isDefault(address)) {
            clearDefault(userId);
        } else if (address.getIsDefault() == null) {
            address.setIsDefault(existing.getIsDefault());
        }

        addressMapper.updateById(address);
        return Result.success();
    }

    @Operation(summary = "Delete address")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedAddress(id, userId);
        addressMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "Set default address")
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedAddress(id, userId);

        clearDefault(userId);
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setIsDefault(1);
        addressMapper.updateById(address);
        return Result.success();
    }

    private Address requireOwnedAddress(Long id, Long userId) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !userId.equals(existing.getUserId())) {
            throw new IllegalArgumentException("Address does not exist");
        }
        return existing;
    }

    private boolean isDefault(Address address) {
        return Integer.valueOf(1).equals(address.getIsDefault());
    }

    private void clearDefault(Long userId) {
        Address clear = new Address();
        clear.setIsDefault(0);
        addressMapper.update(clear, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }
}
