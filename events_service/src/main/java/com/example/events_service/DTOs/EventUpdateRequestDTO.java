package com.example.events_service.DTOs;


import com.example.events_service.enums.EventStatus;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequestDTO {

    private String title;

    private String description;

    private String shortDescription;

    private String image;

    private String location;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private List<Long> availableGroupIds;

    private Boolean registrationRequired;

    private EventStatus status;

    /**
     * Школа заявки и запрошенные баллы — редактируемы только пока мероприятие
     * ещё черновик (moderationStatus=DRAFT), и только владельцем. После отправки
     * на рассмотрение — зафиксированы (см. EventService.updateEvent).
     */
    private Long schoolId;

    @PositiveOrZero
    private Integer requestedPointsPerAttendee;

    /**
     * Фактически назначенные баллы — может менять ТОЛЬКО администратор, в любой
     * момент (включая уже одобренные/прошедшие мероприятия) — на случай ошибки
     * при одобрении (человеческий фактор).
     */
    @PositiveOrZero
    private Integer pointsPerAttendee;
}
