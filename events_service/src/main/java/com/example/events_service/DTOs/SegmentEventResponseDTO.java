package com.example.events_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentEventResponseDTO {

    private UUID segmentEventId;
    private UUID eventId;
    private UUID segmentId;
    private String title;
    private String description;
    private String shortDescription;
    private String geoPoint;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer pointGain;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
