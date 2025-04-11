package com.example.Tredbase_payment_system.Dto;

import lombok.*;


@Data
public class PaymentRequest {
    private Long parentId;
    private Long studentId;
    private Double paymentAmount;
}
