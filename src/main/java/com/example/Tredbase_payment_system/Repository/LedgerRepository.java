package com.example.Tredbase_payment_system.Repository;

import com.example.Tredbase_payment_system.Entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
}
