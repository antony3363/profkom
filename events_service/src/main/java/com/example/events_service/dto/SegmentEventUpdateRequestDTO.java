package com.example.events_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentEventUpdateRequestDTO {

    private String title;

    private String description;

    private String shortDescription;

    private String geoPoint;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer pointGain;
}
