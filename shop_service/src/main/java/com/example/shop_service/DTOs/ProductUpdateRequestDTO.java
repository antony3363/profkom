package com.example.shop_service.DTOs;

import com.example.shop_service.enums.ProductStatus;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDTO {
    private String title;
    private String description;
    private Long price;
    private UUID categoryId;
    private UUID coverMediaId;
    private ProductStatus status;
}
