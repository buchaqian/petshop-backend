package com.petshop.controller.admin;

import com.petshop.common.PageResult;
import com.petshop.common.Result;
import com.petshop.dto.ProductDTO;
import com.petshop.service.ProductService;
import com.petshop.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-商品管理")
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "商品列表（管理端）")
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.adminGetProductList(categoryId, keyword, status, page, size));
    }

    @Operation(summary = "新增商品")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ProductDTO dto) {
        productService.addProduct(dto);
        return Result.success();
    }

    @Operation(summary = "编辑商品")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody ProductDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    @Operation(summary = "上架/下架商品")
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
