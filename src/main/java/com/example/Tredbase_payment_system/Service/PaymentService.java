package com.example.Tredbase_payment_system.Service;

import com.example.Tredbase_payment_system.Entity.Ledger;
import com.example.Tredbase_payment_system.Entity.Parent;
import com.example.Tredbase_payment_system.Entity.Payment;
import com.example.Tredbase_payment_system.Entity.Student;
import com.example.Tredbase_payment_system.Repository.LedgerRepository;
import com.example.Tredbase_payment_system.Repository.ParentRepository;
import com.example.Tredbase_payment_system.Repository.PaymentRepository;
import com.example.Tredbase_payment_system.Repository.StudentRepository;
import com.example.Tredbase_payment_system.Utils.TransactionStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private final ParentRepository parentRepo;
    @Autowired
    private final StudentRepository studentRepo;
    @Autowired
    private final PaymentRepository paymentRepo;
    @Autowired
    private final LedgerRepository ledgerRepo;

    public PaymentService(ParentRepository parentRepo, StudentRepository studentRepo, PaymentRepository paymentRepo, LedgerRepository ledgerRepo) {
        this.parentRepo = parentRepo;
        this.studentRepo = studentRepo;
        this.paymentRepo = paymentRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Transactional
    public void processPayment(Long parentId, Long studentId, Double paymentAmount) {
        Payment payment = new Payment();
        try {
            Parent payingParent = parentRepo.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent not found"));

            Student student = studentRepo.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            double dynamicRate = 0.05;
            double adjustedAmount = paymentAmount * (1 + dynamicRate);

            payment.setParentId(parentId);
            payment.setStudentId(studentId);
            payment.setAmount(paymentAmount);
            payment.setPaymentDate(LocalDateTime.now());

            paymentRepo.save(payment);

            if (student.getParents().size() == 2) {
                for (Parent parent : student.getParents()) {
                    double deduction = adjustedAmount / 2;
                    parent.setBalance(parent.getBalance() - deduction);
                    parentRepo.save(parent);
                }
            } else {
                payingParent.setBalance(payingParent.getBalance() - adjustedAmount);
                parentRepo.save(payingParent);
            }

            student.setBalance(student.getBalance() + paymentAmount);
            studentRepo.save(student);

            // Save success in ledger
            Ledger ledger = new Ledger(payment.getId(),
                    payingParent.getName(),
                    student.getStudentName(),
                    adjustedAmount,
                    TransactionStatus.SUCCESS,
                    LocalDateTime.now());
            ledgerRepo.save(ledger);

        } catch (Exception ex) {
            // Save failure in ledger
            Ledger ledger = new Ledger(null, payment.getId(),
                    "Unknown",
                    "Unknown",
                    paymentAmount,
                    TransactionStatus.FAILED,
                    LocalDateTime.now());
            ledgerRepo.save(ledger);

            throw ex; // trigger rollback
        }
    }

    public List<Student> getStudents() {
        return studentRepo.findAll();
    }

    public List<Ledger> getLedger() {
        return ledgerRepo.findAll();
    }
}
