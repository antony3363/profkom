package com.example.profile_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolCreateRequestDTO {

    @NotBlank
    private String title;
}
