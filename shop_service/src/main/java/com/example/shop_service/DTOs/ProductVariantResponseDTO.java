package com.example.shop_service.DTOs;

import com.example.shop_service.enums.ProductSize;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponseDTO {
    private UUID variantId;
    private UUID productId;
    private ProductSize size;
    private String color;
    private int stock;
    private Long priceOverride;
    private LocalDateTime createdAt;
}
