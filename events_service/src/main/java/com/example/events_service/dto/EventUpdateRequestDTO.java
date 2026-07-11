package com.example.events_service.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequestDTO {

    private String title;

    private String description;

    private String shortDescription;

    private String image;

    private LocalDateTime registrationStartAt;

    private LocalDateTime registrationEndAt;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private List<String> availableGroupIds;
}
