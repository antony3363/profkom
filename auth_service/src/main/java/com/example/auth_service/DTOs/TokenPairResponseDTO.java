package com.example.auth_service.DTOs;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPairResponseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
}
