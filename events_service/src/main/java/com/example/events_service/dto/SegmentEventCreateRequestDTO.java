package com.example.events_service.dto;


import lombok.*;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentEventCreateRequestDTO {

    private UUID eventId;

    private UUID segmentId;

    private String title;

    private String description;

    private String shortDescription;

    private String geoPoint;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer pointGain;
}
