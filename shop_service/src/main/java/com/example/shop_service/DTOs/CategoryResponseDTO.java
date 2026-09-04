package com.example.shop_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private UUID categoryId;
    private UUID parentId;
    private String title;
    private LocalDateTime createdAt;
}
