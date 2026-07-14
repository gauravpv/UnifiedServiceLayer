package com.bajaj.repository;

import com.bajaj.entity.BureauTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BureauTransactionRepository extends JpaRepository<BureauTransaction, Long> {
    Optional<BureauTransaction> findTopByRequestHashOrderByResponseTimestampDesc(String requestHash);
}
