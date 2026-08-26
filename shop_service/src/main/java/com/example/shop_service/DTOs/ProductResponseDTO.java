package com.example.shop_service.DTOs;

import com.example.shop_service.enums.ProductStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private UUID productId;
    private UUID categoryId;
    private String title;
    private String slug;
    private String description;
    private long price;
    private ProductStatus status;
    private UUID coverMediaId;
    private List<ProductVariantResponseDTO> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
