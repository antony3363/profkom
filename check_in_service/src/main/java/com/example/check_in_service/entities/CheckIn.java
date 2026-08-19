package com.example.check_in_service.entities;

import com.example.check_in_service.enums.CheckInType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "check_in_id", nullable = false, updatable = false)
    private UUID checkInId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", updatable = false, referencedColumnName = "registration_id")
    private Registration registration;

    @Column(name = "person_id", nullable = false, updatable = false)
    private long personId;

    @Column(name = "segment_id", nullable = false, updatable = false)
    private UUID segmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private CheckInType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
