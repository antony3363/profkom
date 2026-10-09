package com.example.events_service.DTOs;

import com.example.events_service.enums.EventModerationStatus;
import com.example.events_service.enums.EventStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {

    private UUID eventId;
    private String title;
    private String description;
    private String shortDescription;
    private String image;
    private LocalDateTime registrationStartAt;
    private LocalDateTime registrationEndAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<Long> availableGroupIds;
    private long ownerId;
    private Long schoolId;
    private EventStatus status;
    private EventModerationStatus moderationStatus;
    private Integer requestedPointsPerAttendee;
    private Integer pointsPerAttendee;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private boolean registrationRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
