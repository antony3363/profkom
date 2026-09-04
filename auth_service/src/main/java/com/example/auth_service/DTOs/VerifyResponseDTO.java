package com.example.auth_service.DTOs;

import com.example.auth_service.enums.UserRole;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponseDTO {
    private long personId;
    private UserRole role;
    private Long schoolId;
    private long expiresAtEpochSeconds;
}
