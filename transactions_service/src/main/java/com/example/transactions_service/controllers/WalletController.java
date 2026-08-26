package com.example.transactions_service.controllers;

import com.example.transactions_service.DTOs.WalletResponseDTO;
import com.example.transactions_service.exceptions.UnauthorizedException;
import com.example.transactions_service.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponseDTO> getMyWallet(
            @RequestHeader(value = "X-Person-Id", required = false) Long personId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (personId == null) {
            throw new UnauthorizedException("Missing X-Person-Id header");
        }
        return ResponseEntity.ok(walletService.getMyPersonalWallet(personId, ROLE_ADMIN.equals(role)));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponseDTO> getWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWalletById(walletId));
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<WalletResponseDTO> getSchoolWallet(@PathVariable long schoolId) {
        return ResponseEntity.ok(walletService.getSchoolWallet(schoolId));
    }
}
