package com.example.shop_service.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String slug;

    private String description;

    @NotNull
    @PositiveOrZero
    private Long price;

    private UUID categoryId;

    @NotEmpty
    @Valid
    private List<ProductVariantNestedCreateRequestDTO> variants;
}
