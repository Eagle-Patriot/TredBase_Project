package com.example.Tredbase_payment_system.Entity;

import com.example.Tredbase_payment_system.Enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    private Long parentId;
    private Long studentId;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private LocalDateTime paymentDate;
    private String description;
}
