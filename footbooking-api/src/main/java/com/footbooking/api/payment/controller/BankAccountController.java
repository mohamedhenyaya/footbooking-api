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

import java.util.Map;

@RestController
@RequestMapping("/api/terrains/{terrainId}/bank-account")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<BankAccountDTO> createOrUpdateBankAccount(
            @PathVariable Long terrainId,
            @Valid @RequestBody CreateBankAccountDTO request,
            @AuthenticationPrincipal UserDetails user) {
        BankAccountDTO result = bankAccountService.createOrUpdateBankAccount(terrainId, request, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<BankAccountDTO> getBankAccount(@PathVariable Long terrainId) {
        BankAccountDTO result = bankAccountService.getBankAccountByTerrainId(terrainId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> deleteBankAccount(
            @PathVariable Long terrainId,
            @AuthenticationPrincipal UserDetails user) {
        bankAccountService.deleteBankAccount(terrainId, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Bank account deleted successfully"));
    }
}
