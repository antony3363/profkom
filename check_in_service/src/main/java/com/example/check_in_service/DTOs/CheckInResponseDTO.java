package com.example.check_in_service.DTOs;

import com.example.check_in_service.enums.CheckInType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInResponseDTO {

    private UUID checkInId;
    private UUID registrationId;
    private long personId;
    private UUID eventId;
    private CheckInType type;
    private LocalDateTime createdAt;
}
