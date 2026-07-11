package com.example.events_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "volunteers")
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

    @Column(name = "person_id", nullable = false, updatable = false)
    private UUID personId;
    //@Column(name = "event_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;
    @Column(name = "role")
    private String role;
}
