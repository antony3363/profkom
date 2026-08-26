package com.example.auth_service.DTOs;

import com.example.auth_service.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID userId;
    private long personId;
    private UserRole role;
    private LocalDateTime createdAt;
}
