package com.example.shop_service.entities;

import com.example.shop_service.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_id", nullable = false, updatable = false)
    private UUID purchaseId;

    /**
     * person_id покупателя — ссылка на Profile Service, без FK (другой сервис/БД).
     */
    @Column(name = "buyer_id", nullable = false, updatable = false)
    private long buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false, updatable = false, referencedColumnName = "variant_id")
    private ProductVariant variant;

    /**
     * Снапшот на момент покупки — не зависит от последующих изменений товара.
     */
    @Column(name = "product_title", nullable = false, updatable = false)
    private String productTitle;

    @Column(name = "variant_label", updatable = false)
    private String variantLabel;

    @Column(name = "unit_price", nullable = false, updatable = false)
    private long unitPrice;

    @Column(name = "count", nullable = false, updatable = false)
    private int count;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private PurchaseStatus status = PurchaseStatus.PENDING;

    /**
     * Ссылка на Transaction в Transactions Service — заполняется после подтверждения
     * списания баллов (пока эта интеграция не подключена).
     */
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
