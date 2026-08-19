package com.example.events_service.DTOs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDescriptionResponse {
    private UUID eventId;
    private String title;
    private String shortDescription;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

}
