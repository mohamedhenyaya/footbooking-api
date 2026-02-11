package com.footbooking.api.payment.controller;

import com.footbooking.api.payment.dto.BankAccountDTO;
import com.footbooking.api.payment.dto.CreateBankAccountDTO;
import com.footbooking.api.payment.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Import nécessaire pour la liste
import java.util.Map;

@RestController
// CHANGEMENT 1 : Pluriel pour refléter la liste (/bank-accounts)
@RequestMapping("/api/terrains/{terrainId}/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<BankAccountDTO> addBankAccount(
            @Valid @RequestBody CreateBankAccountDTO request, // On utilise le terrainId du DTO
            @AuthenticationPrincipal UserDetails user) {

        BankAccountDTO result = bankAccountService.addBankAccount(request, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getBankAccounts(@PathVariable Long terrainId) {
        // Retourne maintenant une liste
        List<BankAccountDTO> result = bankAccountService.getBankAccountsByTerrainId(terrainId);
        return ResponseEntity.ok(result);
    }

    // CHANGEMENT 2 : On doit préciser l'ID du compte à supprimer dans l'URL
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> deleteBankAccount(
            @PathVariable Long terrainId, // Gardé pour l'URL, mais pas forcément utilisé par le service
            @PathVariable Long accountId, // L'ID spécifique du compte à supprimer
            @AuthenticationPrincipal UserDetails user) {

        bankAccountService.deleteBankAccount(accountId, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Bank account deleted successfully"));
    }
}