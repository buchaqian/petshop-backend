package com.petshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;
    private String mainImage;
    private String images;

    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;

    private BigDecimal distributorPrice;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private BigDecimal commissionRate1;
    private BigDecimal commissionRate2;
    private Integer status;

    private List<SpecDTO> specs;

    @Data
    public static class SpecDTO {
        private Long id;
        private String specName;
        private BigDecimal price;
        private Integer stock;
    }
}
