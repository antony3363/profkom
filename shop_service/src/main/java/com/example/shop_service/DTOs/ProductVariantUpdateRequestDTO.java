package com.example.shop_service.DTOs;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantUpdateRequestDTO {
    private Integer stock;
    private Long priceOverride;
}
