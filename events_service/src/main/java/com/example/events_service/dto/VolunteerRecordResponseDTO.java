package com.example.events_service.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerRecordResponseDTO {

    private UUID volunteerEntryId;
    private UUID personId;
    private UUID eventId;
    private String role;
}
