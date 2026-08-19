package com.example.events_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCatalogDTO {

    private UUID eventId;
    private String title;
    private String shortDescription;
    private String image;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime registrationEndAt;
}
