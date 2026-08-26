package com.example.transactions_service.services;

import com.example.transactions_service.DTOs.WalletResponseDTO;
import com.example.transactions_service.entities.Wallet;
import com.example.transactions_service.enums.WalletType;
import com.example.transactions_service.exceptions.EntityNotFoundException;
import com.example.transactions_service.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * isAdmin решает вызывающая сторона (контроллер), уже проверившая роль по
     * заголовкам от API Gateway — здесь мы просто доверяем этому флагу при первом
     * создании кошелька. Если кошелёк уже существует, его unlimited-статус не меняется
     * задним числом (роль могла быть присвоена позже — это отдельный сценарий, не
     * покрытый MVP).
     */
    @Transactional
    public Wallet getOrCreatePersonalWallet(long userId, boolean isAdmin) {
        return walletRepository.findByOwnerUserIdAndWalletType(userId, WalletType.PERSONAL)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .walletType(WalletType.PERSONAL)
                        .ownerUserId(userId)
                        .balance(0L)
                        .unlimited(isAdmin)
                        .build()));
    }

    @Transactional
    public Wallet getOrCreateSchoolWallet(long schoolId) {
        return walletRepository.findBySchoolIdAndWalletType(schoolId, WalletType.SCHOOL)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .walletType(WalletType.SCHOOL)
                        .schoolId(schoolId)
                        .balance(0L)
                        .unlimited(false)
                        .build()));
    }

    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletById(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found with id: " + walletId));
        return mapToResponseDTO(wallet);
    }

    @Transactional
    public WalletResponseDTO getMyPersonalWallet(long userId, boolean isAdmin) {
        return mapToResponseDTO(getOrCreatePersonalWallet(userId, isAdmin));
    }

    @Transactional
    public WalletResponseDTO getSchoolWallet(long schoolId) {
        return mapToResponseDTO(getOrCreateSchoolWallet(schoolId));
    }

    public WalletResponseDTO mapToResponseDTO(Wallet wallet) {
        return WalletResponseDTO.builder()
                .walletId(wallet.getWalletId())
                .walletType(wallet.getWalletType())
                .ownerUserId(wallet.getOwnerUserId())
                .schoolId(wallet.getSchoolId())
                .balance(wallet.getBalance())
                .unlimited(wallet.isUnlimited())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
