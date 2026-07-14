package com.bajaj.repository;

import com.bajaj.entity.DedupeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DedupeTransactionRepository extends JpaRepository<DedupeTransaction, Long> {
    Optional<DedupeTransaction> findTopByRequestHashOrderByResponseTimestampDesc(String requestHash);
}
