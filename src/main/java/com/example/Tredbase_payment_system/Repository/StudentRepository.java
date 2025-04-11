package com.example.Tredbase_payment_system.Repository;

import com.example.Tredbase_payment_system.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
