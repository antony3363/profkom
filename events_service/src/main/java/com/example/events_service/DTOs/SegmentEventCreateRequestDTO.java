package com.example.events_service.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentEventCreateRequestDTO {

    @NotNull
    private UUID eventId;

    //private UUID segmentId;

    @NotBlank
    private String title;

    @NotNull
    private String description;


    //private String shortDescription;

    @NotNull
    private String geoPoint;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @PositiveOrZero
    private Integer pointGain;

    @PositiveOrZero
    private Integer orderIndex;
}
