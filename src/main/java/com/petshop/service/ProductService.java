package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.common.PageResult;
import com.petshop.dto.ProductDTO;
import com.petshop.entity.Product;
import com.petshop.vo.ProductVO;

public interface ProductService extends IService<Product> {

    // 用户端
    PageResult<ProductVO> getProductList(Long categoryId, String keyword, Integer page, Integer size);
    ProductVO getProductDetail(Long id);

    // 管理端
    PageResult<ProductVO> adminGetProductList(Long categoryId, String keyword, Integer status, Integer page, Integer size);
    void addProduct(ProductDTO dto);
    void updateProduct(ProductDTO dto);
    void updateStatus(Long id, Integer status);
    void deleteProduct(Long id);
}
