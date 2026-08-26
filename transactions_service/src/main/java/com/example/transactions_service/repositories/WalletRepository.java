package com.example.transactions_service.repositories;

import com.example.transactions_service.entities.Wallet;
import com.example.transactions_service.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByOwnerUserIdAndWalletType(Long ownerUserId, WalletType walletType);
    Optional<Wallet> findBySchoolIdAndWalletType(Long schoolId, WalletType walletType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findWithLockByWalletId(UUID walletId);
}
