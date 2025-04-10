package com.example.Tredbase_payment_system.Entity;

import com.example.Tredbase_payment_system.Utils.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
public class Ledger {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private Long paymentId;
    private String parentName;
    private String studentName;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // SUCCESS, FAILED
    private LocalDateTime timestamp;

    public Ledger(Long id, String name, String studentName, double adjustedAmount, TransactionStatus transactionStatus, LocalDateTime now) {
    }

    public Ledger(String parentName, String studentName, Double amount, TransactionStatus status, LocalDateTime timestamp) {
        this.parentName = parentName;
        this.studentName = studentName;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Ledger() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Ledger(Long id, Long paymentId, String parentName, String studentName, Double amount, TransactionStatus status, LocalDateTime timestamp) {
        this.id = id;
        this.paymentId = paymentId;
        this.parentName = parentName;
        this.studentName = studentName;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Ledger{" +
                "id=" + id +
                ", paymentId=" + paymentId +
                ", parentName='" + parentName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
