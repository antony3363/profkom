package com.example.events_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventModerationDecisionDTO {

    /**
     * Обязательно только для принятия заявки (accept) — сколько баллов получит
     * каждый посетивший мероприятие.
     */
    @NotNull
    @PositiveOrZero
    private Integer pointsPerAttendee;
}
