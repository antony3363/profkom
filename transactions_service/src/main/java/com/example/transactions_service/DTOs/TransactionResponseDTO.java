package com.example.transactions_service.DTOs;

import com.example.transactions_service.enums.TransactionStatus;
import com.example.transactions_service.enums.TransactionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private UUID transactionId;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private TransactionType type;
    private long amount;
    private TransactionStatus status;
    private UUID relatedEventId;
    private UUID relatedPurchaseId;
    private String description;
    private LocalDateTime createdAt;
}
