package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.common.PageResult;
import com.petshop.dto.ProductDTO;
import com.petshop.entity.Category;
import com.petshop.entity.Product;
import com.petshop.entity.ProductSpec;
import com.petshop.mapper.CategoryMapper;
import com.petshop.mapper.ProductMapper;
import com.petshop.mapper.ProductSpecMapper;
import com.petshop.service.ProductService;
import com.petshop.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final ProductSpecMapper productSpecMapper;

    private Map<Long, String> getCategoryMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private ProductVO toVO(Product product, Map<Long, String> categoryMap, boolean withSpecs) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), ""));
        if (withSpecs) {
            List<ProductSpec> specList = productSpecMapper.selectList(
                    new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, product.getId()));
            vo.setSpecs(specList.stream().map(s -> {
                ProductVO.SpecVO sv = new ProductVO.SpecVO();
                sv.setId(s.getId());
                sv.setSpecName(s.getSpecName());
                sv.setPrice(s.getPrice());
                sv.setStock(s.getStock());
                return sv;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    // ========== 用户端 ==========

    @Override
    public PageResult<ProductVO> getProductList(Long categoryId, String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), Product::getName, keyword)
                .orderByDesc(Product::getSales);

        Page<Product> pageResult = this.page(new Page<>(page, size), wrapper);
        Map<Long, String> categoryMap = getCategoryMap();
        List<ProductVO> voList = pageResult.getRecords().stream()
                .map(p -> toVO(p, categoryMap, true)).collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList);
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }
        return toVO(product, getCategoryMap(), true);
    }

    // ========== 管理端 ==========

    @Override
    public PageResult<ProductVO> adminGetProductList(Long categoryId, String keyword, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .like(StringUtils.hasText(keyword), Product::getName, keyword)
                .orderByDesc(Product::getCreateTime);

        Page<Product> pageResult = this.page(new Page<>(page, size), wrapper);
        Map<Long, String> categoryMap = getCategoryMap();
        List<ProductVO> voList = pageResult.getRecords().stream()
                .map(p -> toVO(p, categoryMap, true)).collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList);
    }

    @Override
    @Transactional
    public void addProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        product.setSales(0);
        save(product);

        // 保存规格
        if (dto.getSpecs() != null && !dto.getSpecs().isEmpty()) {
            List<ProductSpec> specs = dto.getSpecs().stream().map(s -> {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(product.getId());
                spec.setSpecName(s.getSpecName());
                spec.setPrice(s.getPrice());
                spec.setStock(s.getStock());
                return spec;
            }).collect(Collectors.toList());
            specs.forEach(productSpecMapper::insert);
        }
    }

    @Override
    @Transactional
    public void updateProduct(ProductDTO dto) {
        if (dto.getId() == null) throw new RuntimeException("商品ID不能为空");
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        updateById(product);

        // 更新规格：先删后增
        if (dto.getSpecs() != null) {
            productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>()
                    .eq(ProductSpec::getProductId, dto.getId()));
            dto.getSpecs().forEach(s -> {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(dto.getId());
                spec.setSpecName(s.getSpecName());
                spec.setPrice(s.getPrice());
                spec.setStock(s.getStock());
                productSpecMapper.insert(spec);
            });
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        removeById(id);
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>()
                .eq(ProductSpec::getProductId, id));
    }
}
