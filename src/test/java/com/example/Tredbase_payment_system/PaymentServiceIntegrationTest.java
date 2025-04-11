package com.example.Tredbase_payment_system;

import com.example.Tredbase_payment_system.Entity.Parent;
import com.example.Tredbase_payment_system.Entity.Payment;
import com.example.Tredbase_payment_system.Entity.Student;
import com.example.Tredbase_payment_system.Repository.ParentRepository;
import com.example.Tredbase_payment_system.Repository.PaymentRepository;
import com.example.Tredbase_payment_system.Repository.StudentRepository;
import com.example.Tredbase_payment_system.Service.PaymentService;
import com.example.Tredbase_payment_system.Enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Test data
    private Parent parentA;
    private Parent parentB;
    private Student sharedStudent;
    private Student studentA;
    private Student studentB;

    @BeforeEach
    void setUp() {
        // Clean up previous data
        paymentRepository.deleteAll();
        studentRepository.deleteAll();
        parentRepository.deleteAll();

        // Parent A
        parentA = new Parent();
        parentA.setName("Parent A");
        parentA.setBalance(500.0);
        parentRepository.save(parentA);

        // Parent B
        parentB = new Parent();
        parentB.setName("Parent B");
        parentB.setBalance(500.0);
        parentRepository.save(parentB);

        // Shared Student
        sharedStudent = new Student();
        sharedStudent.setStudentName("Shared Student");
        sharedStudent.setBalance(0.0);
        // Link both parents
        sharedStudent.setParents(List.of(parentA, parentB));
        studentRepository.save(sharedStudent);

        // Unique Student for Parent A
        studentA = new Student();
        studentA.setStudentName("Student A");
        studentA.setBalance(0.0);
        studentA.setParents(Collections.singletonList(parentA));
        studentRepository.save(studentA);

        // Unique Student for Parent B
        studentB = new Student();
        studentB.setStudentName("Student B");
        studentB.setBalance(0.0);
        studentB.setParents(Collections.singletonList(parentB));
        studentRepository.save(studentB);
    }

    @Test
    @DisplayName("Successful payment for unique student")
    void testProcessPayment_UniqueStudent_Success() {
        double paymentAmount = 100.0;
        paymentService.processPayment(parentA.getId(), studentA.getStudentId(), paymentAmount);

        // Check parent's balance
        Parent updatedA = parentRepository.findById(parentA.getId()).orElseThrow();
        // We expect 500 - (100 * 1.05) = 500 - 105 = 395.0
        assertEquals(395.0, updatedA.getBalance(), 0.001);

        // Check student's balance
        Student updatedStudentA = studentRepository.findById(studentA.getStudentId()).orElseThrow();
        assertEquals(100.0, updatedStudentA.getBalance());

        // Check payment record
        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(1, allPayments.size());
        Payment p = allPayments.get(0);
        assertEquals(TransactionStatus.SUCCESS, p.getStatus());
        assertEquals("Payment processed successfully.", p.getDescription());
    }

    @Test
    @DisplayName("Failed payment when parent not associated with student")
    void testFail_NotAssociated() {
        double paymentAmount = 50.0;
        // Parent A is not associated with Student B
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(parentA.getId(), studentB.getStudentId(), paymentAmount));

        // Check payment logs
        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(1, allPayments.size());

        Payment failedPayment = allPayments.get(0);
        assertEquals(TransactionStatus.FAILED, failedPayment.getStatus());
        // Description should mention "Payment failed: Parent (ID=...) not associated..."
        assertTrue(failedPayment.getDescription().contains("Payment failed: Parent (ID="));
    }

    @Test
    @DisplayName("Successful payment for shared student - cost split")
    void testProcessPayment_SharedStudent_Success() {
        double paymentAmount = 100.0;
        paymentService.processPayment(parentA.getId(), sharedStudent.getStudentId(), paymentAmount);

        // Each parent's balance should be deducted half of adjusted amount
        Parent updatedA = parentRepository.findById(parentA.getId()).orElseThrow();
        Parent updatedB = parentRepository.findById(parentB.getId()).orElseThrow();

        double adjusted = paymentAmount * 1.05;  // 105
        double half = adjusted / 2;              // 52.5

        assertEquals(500.0 - half, updatedA.getBalance(), 0.001);
        assertEquals(500.0 - half, updatedB.getBalance(), 0.001);

        // Student balance
        Student updatedShared = studentRepository.findById(sharedStudent.getStudentId()).orElseThrow();
        assertEquals(100.0, updatedShared.getBalance());

        // Payment record
        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(1, allPayments.size());
        assertEquals(TransactionStatus.SUCCESS, allPayments.get(0).getStatus());
        assertEquals("Payment processed successfully.", allPayments.get(0).getDescription());
    }

    @Test
    @DisplayName("Failed payment - insufficient balance")
    void testFail_InsufficientBalance() {
        // Payment that is definitely more than 500 * 1.05
        double paymentAmount = 600.0;
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(parentA.getId(), studentA.getStudentId(), paymentAmount));

        // Payment record
        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(1, allPayments.size());

        Payment failedPayment = allPayments.get(0);
        assertEquals(TransactionStatus.FAILED, failedPayment.getStatus());
        // Description should mention "Payment failed: Insufficient balance"
        assertTrue(failedPayment.getDescription().contains("Insufficient balance"));
    }
}
