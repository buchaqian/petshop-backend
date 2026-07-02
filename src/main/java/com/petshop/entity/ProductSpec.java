package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_specs")
public class ProductSpec {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private String specName;
    private BigDecimal price;
    private Integer stock;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
