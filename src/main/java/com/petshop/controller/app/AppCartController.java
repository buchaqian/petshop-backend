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
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "App cart APIs")
@RestController
@RequestMapping("/app/cart")
@RequiredArgsConstructor
public class AppCartController {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    @Operation(summary = "List cart items")
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

    @Operation(summary = "Add cart item")
    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long productId,
                            @RequestParam(required = false) Long specId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();
        validateQuantity(quantity);

        Product product = requireActiveProduct(productId);
        ProductSpec spec = specId == null ? null : requireProductSpec(productId, specId);

        Cart existing = cartMapper.selectOne(cartKeyWrapper(userId, productId, specId));
        int newQuantity = quantity;
        if (existing != null && existing.getQuantity() != null) {
            newQuantity += existing.getQuantity();
        }
        validateStock(resolveStock(product, spec), newQuantity);

        if (existing != null) {
            existing.setQuantity(newQuantity);
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

    @Operation(summary = "Update cart item quantity")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestParam Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Cart cart = requireOwnedCart(id, userId);

        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        if (quantity <= 0) {
            cartMapper.deleteById(id);
            return Result.success();
        }

        Product product = requireActiveProduct(cart.getProductId());
        ProductSpec spec = cart.getSpecId() == null ? null : requireProductSpec(cart.getProductId(), cart.getSpecId());
        validateStock(resolveStock(product, spec), quantity);

        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return Result.success();
    }

    @Operation(summary = "Delete cart item")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedCart(id, userId);
        cartMapper.deleteById(id);
        return Result.success();
    }

    private LambdaQueryWrapper<Cart> cartKeyWrapper(Long userId, Long productId, Long specId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        if (specId == null) {
            wrapper.isNull(Cart::getSpecId);
        } else {
            wrapper.eq(Cart::getSpecId, specId);
        }
        return wrapper;
    }

    private Cart requireOwnedCart(Long id, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new IllegalArgumentException("Cart item does not exist");
        }
        return cart;
    }

    private Product requireActiveProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new IllegalArgumentException("Product does not exist or is not available");
        }
        return product;
    }

    private ProductSpec requireProductSpec(Long productId, Long specId) {
        ProductSpec spec = productSpecMapper.selectById(specId);
        if (spec == null || !Objects.equals(productId, spec.getProductId())) {
            throw new IllegalArgumentException("Product spec does not exist");
        }
        return spec;
    }

    private Integer resolveStock(Product product, ProductSpec spec) {
        return spec == null ? product.getStock() : spec.getStock();
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private void validateStock(Integer stock, Integer quantity) {
        if (stock != null && quantity != null && quantity > stock) {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }
}
