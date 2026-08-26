package com.example.auth_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshRequestDTO {

    @NotBlank
    private String refreshToken;
}
