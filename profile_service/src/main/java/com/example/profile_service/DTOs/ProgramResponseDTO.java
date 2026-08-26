package com.example.profile_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramResponseDTO {
    private Long programId;
    private String title;
    private Long schoolId;
    private Long proforgId;
    private LocalDateTime createdAt;
}
