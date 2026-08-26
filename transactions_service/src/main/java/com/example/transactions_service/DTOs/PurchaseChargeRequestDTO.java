package com.example.transactions_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseChargeRequestDTO {

    @NotNull
    private Long buyerUserId;

    @NotNull
    @Positive
    private Long amount;

    @NotNull
    private UUID purchaseId;

    private String description;
}
