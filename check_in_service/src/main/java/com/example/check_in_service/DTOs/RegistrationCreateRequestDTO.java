package com.example.check_in_service.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationCreateRequestDTO {

    @NotNull
    private Long lichnostId;

    @NotNull
    private UUID eventId;
}
