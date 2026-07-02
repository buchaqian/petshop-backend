package com.petshop.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private String avatar;
    /** 角色：0普通用户 1分销商 2店主 */
    private Integer role;
}
