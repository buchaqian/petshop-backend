package com.petshop.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String userNickname;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime;
    private String trackingNo;
    private String trackingCompany;
    private String remark;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO {
        private Long productId;
        private Long specId;
        private String productName;
        private String productImage;
        private String specName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalAmount;
    }
}
