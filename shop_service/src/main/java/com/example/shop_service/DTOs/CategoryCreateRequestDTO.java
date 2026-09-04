package com.example.shop_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequestDTO {

    @NotBlank
    private String title;

    private UUID parentId;
}
