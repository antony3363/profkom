package com.example.shop_service.DTOs;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCatalogDTO {
    private UUID productId;
    private String title;
    private long price;
    private UUID coverMediaId;
    private int totalStock;
}
