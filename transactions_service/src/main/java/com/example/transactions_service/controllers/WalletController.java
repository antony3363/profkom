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
    private static final String ROLE_PROFORG_SCHOOL = "PROFORG_SCHOOL";

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponseDTO> getMyWallet(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long lichnostId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (lichnostId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        return ResponseEntity.ok(walletService.getMyPersonalWallet(lichnostId, ROLE_ADMIN.equals(role)));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponseDTO> getWallet(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long lichnostId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable UUID walletId) {
        WalletResponseDTO wallet = walletService.getWalletById(walletId);
        boolean isOwner = lichnostId != null && lichnostId.equals(wallet.getOwnerUserId());
        boolean isSchoolRep = ROLE_PROFORG_SCHOOL.equals(role) && callerSchoolId != null && callerSchoolId.equals(wallet.getSchoolId());
        if (!ROLE_ADMIN.equals(role) && !isOwner && !isSchoolRep) {
            throw new UnauthorizedException("Недостаточно прав для просмотра этого кошелька");
        }
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<WalletResponseDTO> getSchoolWallet(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable long schoolId) {
        boolean isSchoolRep = ROLE_PROFORG_SCHOOL.equals(role) && callerSchoolId != null && callerSchoolId == schoolId;
        if (!ROLE_ADMIN.equals(role) && !isSchoolRep) {
            throw new UnauthorizedException("Недостаточно прав для просмотра кошелька школы");
        }
        return ResponseEntity.ok(walletService.getSchoolWallet(schoolId));
    }
}
