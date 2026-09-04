package com.example.auth_service.entities;

import com.example.auth_service.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Назван AppUser, а не User — "user" зарезервированное слово в PostgreSQL.
 * personId — тот же идентификатор, что использует Profile Service для UserProfile
 * (общий ключ между Auth и Profile, см. profkom-architecture).
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "person_id", nullable = false, unique = true, updatable = false)
    private long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /**
     * Заполняется только для роли PROFORG_SCHOOL — какой школой распоряжается.
     * Null для остальных ролей.
     */
    @Column(name = "school_id")
    private Long schoolId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
