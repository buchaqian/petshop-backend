package com.petshop.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.PageResult;
import com.petshop.common.Result;
import com.petshop.entity.Category;
import com.petshop.mapper.CategoryMapper;
import com.petshop.service.ProductService;
import com.petshop.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-商品接口")
@RestController
@RequestMapping("/app/product")
@RequiredArgsConstructor
public class AppProductController {

    private final ProductService productService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "获取商品分类列表")
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> list = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
        );
        return Result.success(list);
    }

    @Operation(summary = "获取商品列表")
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.getProductList(categoryId, keyword, page, size));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/detail/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
