package com.example.shop_service.DTOs;

import com.example.shop_service.enums.ProductSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantNestedCreateRequestDTO {

    private ProductSize size;

    private String color;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private Long priceOverride;
}
