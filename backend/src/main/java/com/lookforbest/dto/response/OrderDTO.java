package com.lookforbest.dto.response;

import com.lookforbest.entity.PaymentOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long planId;
    private String planName;
    private BigDecimal amountCny;
    private String paymentMethod;
    private String status;
    private LocalDateTime paidAt;
    private String thirdPartyOrderId;
    private String remark;
    private LocalDateTime createdAt;

    public static OrderDTO from(PaymentOrder order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setPlanId(order.getPlan().getId());
        dto.setPlanName(order.getPlan().getName());
        dto.setAmountCny(order.getAmountCny());
        dto.setPaymentMethod(order.getPaymentMethod().name());
        dto.setStatus(order.getStatus().name());
        dto.setPaidAt(order.getPaidAt());
        dto.setThirdPartyOrderId(order.getThirdPartyOrderId());
        dto.setRemark(order.getRemark());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
