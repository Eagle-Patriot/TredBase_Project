package com.example.Tredbase_payment_system.Service;

import com.example.Tredbase_payment_system.Entity.Parent;
import com.example.Tredbase_payment_system.Entity.Payment;
import com.example.Tredbase_payment_system.Entity.Student;
import com.example.Tredbase_payment_system.Repository.ParentRepository;
import com.example.Tredbase_payment_system.Repository.PaymentRepository;
import com.example.Tredbase_payment_system.Repository.StudentRepository;
import com.example.Tredbase_payment_system.Enums.TransactionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private final ParentRepository parentRepo;
    @Autowired
    private final StudentRepository studentRepo;
    @Autowired
    private final PaymentRepository paymentRepo;
    @Autowired
    private final PaymentLogService paymentLogService;

    /*
      Process a payment from a specific parent to a specific student.
      Ensures that only the student's own parent can pay.
      Splits payment among parents if the student is shared,
      or charges the initiating parent fully if not shared.
     */
    @Transactional
    public void processPayment(Long parentId, Long studentId, Double paymentAmount) {

        Payment successpayment = new Payment();

        try {
            // 1. Validate parent
            Parent payingParent = parentRepo.findById(parentId)
                    .orElseThrow(() -> {
                        String msg = "Parent not found with ID: " + parentId;
                        logger.error(msg);
                        return new IllegalArgumentException(msg);
                    });

            // 2. Validate student
            Student student = studentRepo.findById(studentId)
                    .orElseThrow(() -> {
                        String msg = "Student not found with ID: " + studentId;
                        logger.error(msg);
                        return new IllegalArgumentException(msg);
                    });

            // 3. Check if parent is associated with that student
            boolean isAssociated = student.getParents()
                    .stream()
                    .anyMatch(p -> p.getId().equals(payingParent.getId()));
            if (!isAssociated) {
                String msg = String.format("Parent (ID=%d) not associated with Student (ID=%d).",
                        parentId, studentId);
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }

            // 4. Validate payment amount
            double dynamicRate = 0.05;
            double adjustedAmount = paymentAmount * (1 + dynamicRate);
            logger.info("Payment request: parentId={}, studentId={}, paymentAmount={}, adjustedAmount={}",
                    parentId, studentId, paymentAmount, adjustedAmount);

            // 5. Check if parent has sufficient balance
            if (payingParent.getBalance() < adjustedAmount) {
                String msg = "Insufficient balance for parent ID: " + parentId;
                logger.warn(msg);
                throw new IllegalArgumentException(msg);
            }

            // 6. Deduct amount from parent(s)
            if (student.getParents().size() == 2) {
                // Shared student => split the payment among the two parents
                for (Parent parent : student.getParents()) {
                    double halfDeduction = adjustedAmount / 2;
                    if (parent.getBalance() < halfDeduction) {
                        String msg = String.format("Insufficient balance in one of the shared parents (ID=%d).",
                                parent.getId());
                        logger.warn(msg);
                        throw new IllegalArgumentException(msg);
                    }
                    parent.setBalance(parent.getBalance() - halfDeduction);
                    parentRepo.save(parent);
                    logger.info("Deducted {} from shared Parent (ID={}). New balance={}",
                            halfDeduction, parent.getId(), parent.getBalance());
                }
            } else {
                // Unique student => only the paying parent is charged
                payingParent.setBalance(payingParent.getBalance() - adjustedAmount);
                parentRepo.save(payingParent);
                logger.info("Deducted {} from Parent (ID={}). New balance={}",
                        adjustedAmount, payingParent.getId(), payingParent.getBalance());
            }

            // 7. Update student's balance
            double oldStudentBalance = student.getBalance();
            student.setBalance(student.getBalance() + paymentAmount);
            studentRepo.save(student);
            logger.info("Updated Student (ID={}) balance from {} to {}",
                    studentId, oldStudentBalance, student.getBalance());

            // 8. Record successful payment
            successpayment.setParentId(parentId);
            successpayment.setStudentId(studentId);
            successpayment.setAmount(paymentAmount);
            successpayment.setStatus(TransactionStatus.SUCCESS);
            successpayment.setPaymentDate(LocalDateTime.now());
            successpayment.setDescription("Payment processed successfully.");
            paymentRepo.save(successpayment);

            logger.info("Payment processed successfully. Payment record created with ID={}", successpayment.getId());

        } catch (Exception ex) {
            // 9. Handle exceptions and rollback
            Payment failedPayment = new Payment();
            failedPayment.setParentId(parentId);
            failedPayment.setStudentId(studentId);
            failedPayment.setAmount(paymentAmount);
            failedPayment.setPaymentDate(LocalDateTime.now());
            failedPayment.setStatus(TransactionStatus.FAILED);
            failedPayment.setDescription("Payment failed: " + ex.getMessage());
            paymentLogService.logPayment(failedPayment);

            logger.error("Payment processing failed. Reason: {}", ex.getMessage(), ex);
            // Rethrow to trigger rollback
            throw ex;
        }
    }

    public List<Student> getStudents() {
        return studentRepo.findAll();
    }

    public List<Payment> getPayments() {
        return paymentRepo.findAll();
    }
}
