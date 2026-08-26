package com.example.profile_service.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Общий DTO для назначения профорга школе/направлению/группе. Права (если это школа)
 * передаются вместе с должностью — прежний профорг ничего не теряет физически, просто
 * перестаёт быть referenced здесь.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProforgAssignRequestDTO {

    @NotNull
    private Long proforgId;
}
