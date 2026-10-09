package com.example.events_service.entities;

import com.example.events_service.enums.EventModerationStatus;
import com.example.events_service.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events", uniqueConstraints = @UniqueConstraint(
        name = "uq_events_title_start_end_school",
        columnNames = {"title", "start_at", "end_at", "school_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "image")
    private String image;

    @Column(name = "registration_start_at", nullable = false)
    private LocalDateTime registrationStartAt;

    @Column(name = "registration_end_at", nullable = false)
    private LocalDateTime registrationEndAt;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "available_group_ids", columnDefinition = "BIGINT[]")
    private List<Long> availableGroupIds;

    /**
     * Профорг школы, отправивший заявку на мероприятие.
     */
    @Column(name = "owner_id", nullable = false)
    private long ownerId;

    /**
     * Школа, от лица которой подана заявка. Null — системное/служебное мероприятие,
     * не показывается в каталоге пользователям.
     */
    @Column(name = "school_id")
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    /**
     * Статус рассмотрения заявки Литвиновым — независим от status (публикации).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    @Builder.Default
    private EventModerationStatus moderationStatus = EventModerationStatus.SUBMITTED;

    /**
     * Сколько баллов за посещение запросил профорг школы при подаче заявки.
     */
    @Column(name = "requested_points_per_attendee")
    private Integer requestedPointsPerAttendee;

    /**
     * Сколько баллов за посещение фактически назначил Литвинов при принятии заявки.
     */
    @Column(name = "points_per_attendee")
    private Integer pointsPerAttendee;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_registration_required", nullable = false)
    private boolean registrationRequired;
}
