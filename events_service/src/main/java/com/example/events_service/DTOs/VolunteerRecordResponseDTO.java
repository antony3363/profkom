package com.example.events_service.DTOs;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerRecordResponseDTO {

    private UUID volunteerEntryId;
    private long lichnostId;
    private UUID eventId;
    private String fullName;
    private String faculty;
}
