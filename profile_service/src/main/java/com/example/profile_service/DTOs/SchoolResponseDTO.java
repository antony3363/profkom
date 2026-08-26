package com.example.profile_service.DTOs;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolResponseDTO {
    private Long schoolId;
    private String title;
    private Long proforgId;
    private LocalDateTime createdAt;
}
