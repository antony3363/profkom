package com.example.events_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "volunteers", uniqueConstraints = @UniqueConstraint(columnNames = {"lichnost_id", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class VolunteerRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "volunteer_record_id", nullable = false, updatable = false)
    private UUID volunteerRecordId;

    @Column(name = "lichnost_id", nullable = false, updatable = false)
    private long lichnostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;
}
