package com.example.transactions_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRewardRequestDTO {

    @NotNull
    private Long receiverUserId;

    @NotNull
    @Positive
    private Long amount;

    @NotNull
    private UUID eventId;

    private String description;
}
