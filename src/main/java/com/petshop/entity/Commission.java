package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("commissions")
public class Commission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long orderId;
    private String orderNo;
    private BigDecimal amount;

    /** 佣金层级：1一级 2二级 */
    private Integer level;

    /** 状态：0待结算 1已结算 */
    private Integer status;

    private LocalDateTime settleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
