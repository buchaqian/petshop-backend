package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;
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

    /** 状态：1上架 0下架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
