package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;
}
