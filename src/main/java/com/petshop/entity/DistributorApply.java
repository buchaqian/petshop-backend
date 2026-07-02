package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("distributor_apply")
public class DistributorApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String realName;
    private String phone;

    /** 申请状态：0待审核 1已通过 2已拒绝 */
    private Integer status;

    private String reason;
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
