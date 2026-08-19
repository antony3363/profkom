package com.example.events_service.DTOs;

import com.example.events_service.enums.AttendanceTypes;
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
    private List<String> availableGroupIds;
    private long ownerId;
    private EventStatus status;
    private boolean registrationRequired;
    private AttendanceTypes attendanceType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SegmentEventResponseDTO> segmentEvents;
}
