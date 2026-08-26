package com.example.shop_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseCreateRequestDTO {

    @NotNull
    private UUID variantId;

    @NotNull
    @Positive
    private Integer count;
}
