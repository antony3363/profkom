package com.example.profile_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponseDTO {
    private Long groupId;
    private String title;
    private Long programId;
    private Long proforgId;
    private LocalDateTime createdAt;
}
