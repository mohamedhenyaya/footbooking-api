package com.footbooking.api.payment.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.payment.dto.BankAccountDTO;
import com.footbooking.api.payment.dto.CreateBankAccountDTO;
import com.footbooking.api.payment.exception.BankAccountNotFoundException;
import com.footbooking.api.payment.model.BankAccount;
import com.footbooking.api.payment.repository.BankAccountRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;

    public BankAccountDTO createOrUpdateBankAccount(Long terrainId, CreateBankAccountDTO dto, String adminEmail) {
        // Verify terrain exists
        Terrain terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

        // Verify admin owns the terrain
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + adminEmail));

        if (terrain.getOwner() == null || !terrain.getOwner().getId().equals(admin.getId())) {
            // Check if superadmin
            boolean isSuperAdmin = admin.getRoles().stream()
                    .anyMatch(r -> r.getName().contains("SUPERADMIN"));
            if (!isSuperAdmin) {
                throw new RuntimeException("Unauthorized: You do not own this terrain");
            }
        }

        // Check if bank account already exists
        BankAccount bankAccount = bankAccountRepository.findByTerrainId(terrainId)
                .orElse(new BankAccount());

        // Update fields
        bankAccount.setTerrainId(terrainId);
        bankAccount.setAccountHolderName(dto.accountHolderName());
        bankAccount.setBankName(dto.bankName());
        bankAccount.setAccountNumber(dto.accountNumber());
        bankAccount.setRib(dto.rib());
        bankAccount.setAdditionalInfo(dto.additionalInfo());

        BankAccount saved = bankAccountRepository.save(bankAccount);

        return new BankAccountDTO(
                saved.getId(),
                saved.getAccountHolderName(),
                saved.getBankName(),
                saved.getAccountNumber(),
                saved.getRib(),
                saved.getAdditionalInfo());
    }

    public BankAccountDTO getBankAccountByTerrainId(Long terrainId) {
        BankAccount bankAccount = bankAccountRepository.findByTerrainId(terrainId)
                .orElseThrow(() -> new BankAccountNotFoundException(terrainId));

        return new BankAccountDTO(
                bankAccount.getId(),
                bankAccount.getAccountHolderName(),
                bankAccount.getBankName(),
                bankAccount.getAccountNumber(),
                bankAccount.getRib(),
                bankAccount.getAdditionalInfo());
    }

    public void deleteBankAccount(Long terrainId, String adminEmail) {
        // Verify terrain exists
        Terrain terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

        // Verify admin owns the terrain
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + adminEmail));

        if (terrain.getOwner() == null || !terrain.getOwner().getId().equals(admin.getId())) {
            // Check if superadmin
            boolean isSuperAdmin = admin.getRoles().stream()
                    .anyMatch(r -> r.getName().contains("SUPERADMIN"));
            if (!isSuperAdmin) {
                throw new RuntimeException("Unauthorized: You do not own this terrain");
            }
        }

        BankAccount bankAccount = bankAccountRepository.findByTerrainId(terrainId)
                .orElseThrow(() -> new BankAccountNotFoundException(terrainId));

        bankAccountRepository.delete(bankAccount);
    }
}
