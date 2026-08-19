package com.example.check_in_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registrations", uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "registration_id", nullable = false, updatable = false)
    private UUID registrationId;

    @Column(name = "person_id", nullable = false, updatable = false)
    private long personId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
