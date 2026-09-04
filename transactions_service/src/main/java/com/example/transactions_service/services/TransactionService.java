package com.example.transactions_service.services;

import com.example.transactions_service.DTOs.*;
import com.example.transactions_service.entities.Transaction;
import com.example.transactions_service.entities.Wallet;
import com.example.transactions_service.enums.TransactionType;
import com.example.transactions_service.exceptions.EntityNotFoundException;
import com.example.transactions_service.exceptions.InsufficientBalanceException;
import com.example.transactions_service.repositories.TransactionRepository;
import com.example.transactions_service.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    /**
     * Начисление баллов за посещение мероприятия. Списывается с личного кошелька
     * рецензента (Литвинова/админа, одобрившего мероприятие) — его кошелёк unlimited,
     * поэтому баланс физически не проверяется.
     */
    @Transactional
    public TransactionResponseDTO eventReward(long reviewerUserId, EventRewardRequestDTO dto) {
        Wallet sender = walletService.getOrCreatePersonalWallet(reviewerUserId, true);
        Wallet receiver = walletService.getOrCreatePersonalWallet(dto.getReceiverUserId(), false);

        debit(sender.getWalletId(), dto.getAmount());
        credit(receiver.getWalletId(), dto.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .type(TransactionType.EVENT_REWARD)
                .amount(dto.getAmount())
                .relatedEventId(dto.getEventId())
                .description(dto.getDescription())
                .build();

        return mapToResponseDTO(transactionRepository.saveAndFlush(transaction));
    }

    /**
     * Ручной перевод от Литвинова конкретному человеку или на кошелёк школы.
     * callerUserId уже проверен контроллером как ADMIN.
     */
    @Transactional
    public TransactionResponseDTO transferFromAdmin(long callerUserId, TransferRequestDTO dto) {
        Wallet sender = walletService.getOrCreatePersonalWallet(callerUserId, true);
        Wallet receiver = resolveTransferReceiver(dto);

        debit(sender.getWalletId(), dto.getAmount());
        credit(receiver.getWalletId(), dto.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .type(TransactionType.TRANSFER)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();

        return mapToResponseDTO(transactionRepository.saveAndFlush(transaction));
    }

    /**
     * Профорг школы награждает студента за заслуги из рабочего кошелька школы.
     * Баланс школьного кошелька не unlimited — проверяется как обычно.
     */
    @Transactional
    public TransactionResponseDTO schoolAward(long schoolId, SchoolAwardRequestDTO dto) {
        Wallet sender = walletService.getOrCreateSchoolWallet(schoolId);
        Wallet receiver = walletService.getOrCreatePersonalWallet(dto.getReceiverUserId(), false);

        debit(sender.getWalletId(), dto.getAmount());
        credit(receiver.getWalletId(), dto.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .type(TransactionType.TRANSFER)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();

        return mapToResponseDTO(transactionRepository.saveAndFlush(transaction));
    }

    /**
     * Списание за покупку в Shop Service — баллы выходят из оборота (receiver = null).
     */
    @Transactional
    public TransactionResponseDTO chargePurchase(PurchaseChargeRequestDTO dto) {
        Wallet sender = walletService.getOrCreatePersonalWallet(dto.getBuyerUserId(), false);

        debit(sender.getWalletId(), dto.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(null)
                .type(TransactionType.PURCHASE)
                .amount(dto.getAmount())
                .relatedPurchaseId(dto.getPurchaseId())
                .description(dto.getDescription())
                .build();

        return mapToResponseDTO(transactionRepository.saveAndFlush(transaction));
    }

    /**
     * Возврат за отменённую/возвращённую покупку — баллы возвращаются в оборот
     * (sender = null).
     */
    @Transactional
    public TransactionResponseDTO refundPurchase(PurchaseChargeRequestDTO dto) {
        Wallet receiver = walletService.getOrCreatePersonalWallet(dto.getBuyerUserId(), false);

        credit(receiver.getWalletId(), dto.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(null)
                .receiverWallet(receiver)
                .type(TransactionType.REFUND)
                .amount(dto.getAmount())
                .relatedPurchaseId(dto.getPurchaseId())
                .description(dto.getDescription())
                .build();

        return mapToResponseDTO(transactionRepository.saveAndFlush(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getHistoryByWalletId(UUID walletId) {
        return transactionRepository
                .findBySenderWallet_WalletIdOrReceiverWallet_WalletIdOrderByCreatedAtDesc(walletId, walletId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Wallet resolveTransferReceiver(TransferRequestDTO dto) {
        if (dto.getReceiverUserId() != null) {
            return walletService.getOrCreatePersonalWallet(dto.getReceiverUserId(), false);
        }
        if (dto.getReceiverSchoolId() != null) {
            return walletService.getOrCreateSchoolWallet(dto.getReceiverSchoolId());
        }
        throw new IllegalArgumentException("Either receiverUserId or receiverSchoolId must be set");
    }

    private void debit(UUID walletId, long amount) {
        Wallet wallet = walletRepository.findWithLockByWalletId(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found with id: " + walletId));
        if (!wallet.isUnlimited() && wallet.getBalance() < amount) {
            throw new InsufficientBalanceException(
                    "Insufficient balance on wallet " + walletId + ": has " + wallet.getBalance() + ", needs " + amount);
        }
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);
    }

    private void credit(UUID walletId, long amount) {
        Wallet wallet = walletRepository.findWithLockByWalletId(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found with id: " + walletId));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .senderWalletId(transaction.getSenderWallet() != null ? transaction.getSenderWallet().getWalletId() : null)
                .receiverWalletId(transaction.getReceiverWallet() != null ? transaction.getReceiverWallet().getWalletId() : null)
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .relatedEventId(transaction.getRelatedEventId())
                .relatedPurchaseId(transaction.getRelatedPurchaseId())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
