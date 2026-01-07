package com.footbooking.api.payment.repository;

import com.footbooking.api.payment.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByTerrainId(Long terrainId);

    boolean existsByTerrainId(Long terrainId);
}
