package com.example.transactions_service.DTOs;

import com.example.transactions_service.enums.WalletType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {
    private UUID walletId;
    private WalletType walletType;
    private Long ownerUserId;
    private Long schoolId;
    private long balance;
    private boolean unlimited;
    private LocalDateTime updatedAt;
}
