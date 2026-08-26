package com.example.transactions_service.repositories;

import com.example.transactions_service.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySenderWallet_WalletIdOrReceiverWallet_WalletIdOrderByCreatedAtDesc(
            UUID senderWalletId, UUID receiverWalletId);
}
