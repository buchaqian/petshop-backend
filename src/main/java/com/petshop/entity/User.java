package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;
    private String nickname;
    private String avatar;
    private String phone;

    /** 角色：0普通用户 1分销商 2店主 */
    private Integer role;

    /** 分销等级：0普通 1高级 */
    private Integer distributorLevel;

    private Long parentId;
    private BigDecimal balance;
    private BigDecimal totalCommission;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
