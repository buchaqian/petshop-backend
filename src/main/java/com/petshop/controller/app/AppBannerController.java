package com.petshop.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.entity.Banner;
import com.petshop.mapper.BannerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端-轮播图")
@RestController
@RequestMapping("/app/banner")
@RequiredArgsConstructor
public class AppBannerController {

    private final BannerMapper bannerMapper;

    @Operation(summary = "获取轮播图列表")
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        List<Banner> banners = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSort));
        return Result.success(banners);
    }
}
