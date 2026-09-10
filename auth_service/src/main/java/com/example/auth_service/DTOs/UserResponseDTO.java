package com.example.auth_service.DTOs;

import com.example.auth_service.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long userId;
    private long lichnostId;
    private UserRole role;
    private Long schoolId;
    private LocalDateTime createdAt;
}
