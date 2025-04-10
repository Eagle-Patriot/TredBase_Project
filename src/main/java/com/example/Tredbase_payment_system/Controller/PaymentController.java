package com.example.Tredbase_payment_system.Controller;


import com.example.Tredbase_payment_system.Dto.PaymentRequest;
import com.example.Tredbase_payment_system.Entity.Ledger;
import com.example.Tredbase_payment_system.Entity.Parent;
import com.example.Tredbase_payment_system.Entity.Student;
import com.example.Tredbase_payment_system.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping(path = "/")
    public String Welcome() {
        return "Payment Service is running";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/api_payment")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest) {

        paymentService.processPayment(
                paymentRequest.getParentId(),
                paymentRequest.getStudentId(),
                paymentRequest.getPaymentAmount()
        );
        return ResponseEntity.ok("Payment processed successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/students")
    public List<Student> getStudents() {
        return paymentService.getStudents();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/ledgers")
    public List<Ledger> getTransactionHistory() {
        return paymentService.getLedger();
    }

}
