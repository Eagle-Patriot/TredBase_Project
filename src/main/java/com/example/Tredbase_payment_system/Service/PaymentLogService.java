package com.example.Tredbase_payment_system.Service;

import com.example.Tredbase_payment_system.Entity.Payment;
import com.example.Tredbase_payment_system.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentLogService {
    @Autowired
    private PaymentRepository paymentRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPayment(Payment payment) {
        paymentRepo.save(payment);  // always commits, even if main transaction fails
    }
}
