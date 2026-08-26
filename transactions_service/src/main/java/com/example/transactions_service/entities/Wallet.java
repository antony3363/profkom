package com.example.transactions_service.entities;

import com.example.transactions_service.enums.WalletType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PERSONAL — личный кошелёк (ownerUserId), SCHOOL — рабочий кошелёк школы (schoolId),
 * которым распоряжается её профорг. Postgres допускает несколько NULL в уникальном
 * индексе, поэтому оба ограничения ниже корректно работают одновременно для двух типов
 * кошельков в одной таблице.
 */
@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wallet_owner", columnNames = {"owner_user_id", "wallet_type"}),
        @UniqueConstraint(name = "uk_wallet_school", columnNames = {"school_id", "wallet_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false, updatable = false)
    private WalletType walletType;

    @Column(name = "owner_user_id", updatable = false)
    private Long ownerUserId;

    @Column(name = "school_id", updatable = false)
    private Long schoolId;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private long balance = 0L;

    /**
     * Кошелёк Литвинова (ADMIN) — баланс не проверяется на неотрицательность при
     * списании. Устанавливается один раз при создании кошелька вызывающей стороной,
     * которая уже проверила роль (см. WalletService.getOrCreatePersonalWallet).
     */
    @Column(name = "unlimited", nullable = false)
    @Builder.Default
    private boolean unlimited = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
