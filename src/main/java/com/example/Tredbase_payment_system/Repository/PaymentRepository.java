package com.example.Tredbase_payment_system.Repository;

import com.example.Tredbase_payment_system.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByParentId(Long parentId);

    List<Payment> findByStudentId(Long studentId);
}
