package com.footbooking.api.payment.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.payment.dto.BankAccountDTO;
import com.footbooking.api.payment.dto.CreateBankAccountDTO;
import com.footbooking.api.payment.model.BankAccount;
import com.footbooking.api.payment.repository.BankAccountRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;

    // CHANGEMENT 1 : On AJOUTE un compte (on ne "remplace" plus l'ancien)
    public BankAccountDTO addBankAccount(CreateBankAccountDTO dto, String adminEmail) {
        // Vérification sécurité (via méthode helper pour éviter la duplication)
        verifyOwnership(dto.terrainId(), adminEmail);

        // Création d'un NOUVEAU compte
        BankAccount bankAccount = new BankAccount();
        bankAccount.setTerrainId(dto.terrainId());
        bankAccount.setBankName(dto.bankName());
        bankAccount.setAccountNumber(dto.accountNumber());
        bankAccount.setAdditionalInfo(dto.additionalInfo());

        BankAccount saved = bankAccountRepository.save(bankAccount);
        return mapToDto(saved);
    }

    // CHANGEMENT 2 : On renvoie une LISTE de comptes
    public List<BankAccountDTO> getBankAccountsByTerrainId(Long terrainId) {
        // findByTerrainId renvoie maintenant une List<BankAccount>
        return bankAccountRepository.findByTerrainId(terrainId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // CHANGEMENT 3 : On supprime un compte spécifique par SON id (et pas l'id du terrain)
    public void deleteBankAccount(Long bankAccountId, String adminEmail) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // On vérifie que l'admin est bien propriétaire du terrain lié à ce compte
        verifyOwnership(bankAccount.getTerrainId(), adminEmail);

        bankAccountRepository.delete(bankAccount);
    }

    // Méthode utilitaire pour transformer en DTO
    private BankAccountDTO mapToDto(BankAccount saved) {
        return new BankAccountDTO(
                saved.getId(),
                saved.getTerrainId(),
                saved.getBankName(),
                saved.getAccountNumber(),
                saved.getAdditionalInfo());
    }

    // Méthode utilitaire pour vérifier les droits (Refactoring pour nettoyer le code)
    private void verifyOwnership(Long terrainId, String adminEmail) {
        Terrain terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + adminEmail));

        boolean isSuperAdmin = admin.getRoles().stream()
                .anyMatch(r -> r.getName().contains("SUPERADMIN"));

        if (!isSuperAdmin) {
            if (terrain.getOwner() == null || !terrain.getOwner().getId().equals(admin.getId())) {
                throw new RuntimeException("Unauthorized: You do not own this terrain");
            }
        }
    }
}