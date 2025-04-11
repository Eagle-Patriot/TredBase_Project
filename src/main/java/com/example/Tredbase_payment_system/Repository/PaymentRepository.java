package com.example.Tredbase_payment_system.Repository;

import com.example.Tredbase_payment_system.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
