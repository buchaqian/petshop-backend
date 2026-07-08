package com.petshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {

    @NotNull(message = "Address is required")
    private Long addressId;

    @NotEmpty(message = "Order items are required")
    private List<OrderItemDTO> items;

    private String remark;

    /** Distributor source id passed from a promotion link. */
    private Long fromDistributorId;

    @Data
    public static class OrderItemDTO {

        @NotNull(message = "Product id is required")
        private Long productId;

        private Long specId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        private Integer quantity;
    }
}
