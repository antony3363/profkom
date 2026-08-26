package com.example.transactions_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolAwardRequestDTO {

    @NotNull
    private Long receiverUserId;

    @NotNull
    @Positive
    private Long amount;

    private String description;
}
