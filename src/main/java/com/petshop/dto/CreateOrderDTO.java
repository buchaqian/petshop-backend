package com.petshop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDTO {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "商品不能为空")
    private List<OrderItemDTO> items;

    private String remark;

    /** 推广来源分销商ID（通过分销链接进来时传入） */
    private Long fromDistributorId;

    @Data
    public static class OrderItemDTO {
        @NotNull
        private Long productId;
        private Long specId;
        @NotNull
        private Integer quantity;
    }
}
