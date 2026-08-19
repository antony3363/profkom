package com.example.events_service.DTOs;


import com.example.events_service.enums.AttendanceTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequestDTO {

    @NotBlank
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String shortDescription;

    private String image;

    @NotNull
    private LocalDateTime registrationStartAt;

    @NotNull
    private LocalDateTime registrationEndAt;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private List<String> availableGroupIds;

    @NotNull
    private long ownerId;

    @NotNull
    private AttendanceTypes attendanceType;

    private boolean registrationRequired;

    private List<SegmentEventNestedCreateRequestDTO> segmentEvents;
}
