package com.example.check_in_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDTO {

    private UUID registrationId;
    private long personId;
    private UUID eventId;
    private LocalDateTime createdAt;
}
