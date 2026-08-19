package com.example.events_service.DTOs;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerRecordCreateRequestDTO {

    @NotNull
    private Long lichnostId;

    @NotNull
    private UUID eventId;
}
