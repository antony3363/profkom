package com.example.events_service.dto;

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
    private List<String> availableGroupIds;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SegmentEventResponseDTO> segmentEvents;
}
