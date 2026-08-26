package com.example.profile_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramCreateRequestDTO {

    @NotBlank
    private String title;

    @NotNull
    private Long schoolId;
}
