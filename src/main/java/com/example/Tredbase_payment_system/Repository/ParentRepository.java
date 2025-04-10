package com.example.Tredbase_payment_system.Repository;

import com.example.Tredbase_payment_system.Entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
}
