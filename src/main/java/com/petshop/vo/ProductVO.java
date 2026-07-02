package com.petshop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String mainImage;
    private String images;
    private BigDecimal price;
    private BigDecimal distributorPrice;
    private Integer stock;
    private Integer sales;
    private BigDecimal commissionRate1;
    private BigDecimal commissionRate2;
    private Integer status;

    private List<SpecVO> specs;

    @Data
    public static class SpecVO {
        private Long id;
        private String specName;
        private BigDecimal price;
        private Integer stock;
    }
}
