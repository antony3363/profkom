package com.example.transactions_service.entities;

import com.example.transactions_service.enums.TransactionStatus;
import com.example.transactions_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    /**
     * Null — баллы входят в оборот извне (например REFUND).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id", updatable = false, referencedColumnName = "wallet_id")
    private Wallet senderWallet;

    /**
     * Null — баллы выходят из оборота (например PURCHASE, обменены на реальный товар).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id", updatable = false, referencedColumnName = "wallet_id")
    private Wallet receiverWallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @Column(name = "related_event_id", updatable = false)
    private UUID relatedEventId;

    @Column(name = "related_purchase_id", updatable = false)
    private UUID relatedPurchaseId;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
