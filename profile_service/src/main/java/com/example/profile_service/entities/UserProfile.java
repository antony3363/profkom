package com.example.profile_service.entities;

import com.example.profile_service.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * lichnost_id не генерируется здесь — приходит извне от Auth Service, либо через
 * событие UserRegistered в Kafka (автоматически, если пришли email/имя), либо
 * явно через POST /profiles (ручной путь, когда SSO их не прислала).
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "lichnost_id", nullable = false, updatable = false)
    private Long lichnostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    private Group group;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "card_number", unique = true)
    private String cardNumber;

    @Column(name = "image")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false)
    @Builder.Default
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
