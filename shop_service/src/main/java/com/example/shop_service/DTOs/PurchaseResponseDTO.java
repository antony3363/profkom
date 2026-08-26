package com.example.shop_service.DTOs;

import com.example.shop_service.enums.PurchaseStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponseDTO {
    private UUID purchaseId;
    private long buyerId;
    private UUID variantId;
    private String productTitle;
    private String variantLabel;
    private long unitPrice;
    private int count;
    private long amount;
    private PurchaseStatus status;
    private UUID transactionId;
    private String description;
    private LocalDateTime createdAt;
}
