package com.example.shop_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMediaResponseDTO {
    private UUID mediaId;
    private UUID productId;
    private String media;
    private boolean isCover;
    private int sortOrder;
    private LocalDateTime createdAt;
}
