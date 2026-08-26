package com.example.shop_service.entities;

import com.example.shop_service.enums.ProductSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Любой товар покупается через вариант, даже если у него нет размера/цвета
 * (тогда size и color остаются null) — единая модель без ветвления
 * "одежда/не одежда" в коде покупки.
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false, referencedColumnName = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "size")
    private ProductSize size;

    @Column(name = "color")
    private String color;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "price_override")
    private Long priceOverride;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
