package com.petshop.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DistributorApplyVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String userPhone;
    private Integer status;
    private String statusText;
    private String reason;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;
}
