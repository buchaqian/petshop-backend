package com.petshop.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.entity.Cart;
import com.petshop.entity.Product;
import com.petshop.entity.ProductSpec;
import com.petshop.mapper.CartMapper;
import com.petshop.mapper.ProductMapper;
import com.petshop.mapper.ProductSpecMapper;
import com.petshop.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "用户端-购物车")
@RestController
@RequestMapping("/app/cart")
@RequiredArgsConstructor
public class AppCartController {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    @Operation(summary = "获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        List<CartVO> voList = carts.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSpecId(cart.getSpecId());

            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getMainImage());
                vo.setStatus(product.getStatus());
                vo.setPrice(product.getPrice());
                vo.setStock(product.getStock());
            }
            if (cart.getSpecId() != null) {
                ProductSpec spec = productSpecMapper.selectById(cart.getSpecId());
                if (spec != null) {
                    vo.setSpecName(spec.getSpecName());
                    vo.setPrice(spec.getPrice());
                    vo.setStock(spec.getStock());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long productId,
                            @RequestParam(required = false) Long specId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查是否已存在
        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(specId != null, Cart::getSpecId, specId));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setSpecId(specId);
            cart.setQuantity(quantity);
            cartMapper.insert(cart);
        }
        return Result.success();
    }

    @Operation(summary = "更新购物车数量")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestParam Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (quantity <= 0) {
            cartMapper.deleteById(id);
        } else {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
        return Result.success();
    }

    @Operation(summary = "删除购物车商品")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车记录不存在");
        }
        cartMapper.deleteById(id);
        return Result.success();
    }
}
