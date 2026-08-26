package com.example.shop_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMediaCreateRequestDTO {

    @NotNull
    private UUID productId;

    @NotBlank
    private String media;

    private boolean isCover;

    private int sortOrder;
}
