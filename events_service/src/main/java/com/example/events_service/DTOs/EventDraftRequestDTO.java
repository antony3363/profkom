package com.example.events_service.DTOs;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сохранение/дозаполнение черновика — в отличие от EventCreateRequestDTO здесь
 * нет обязательных полей вообще: профорг может сохранить заявку в любой момент
 * заполнения и вернуться позже. Полнота проверяется только при явной отправке
 * на рассмотрение (EventService.submitDraft).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDraftRequestDTO {

    private String title;

    private String description;

    private String shortDescription;

    private String image;

    private String location;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private List<Long> availableGroupIds;

    /**
     * Школа, от лица которой готовится заявка. Null допустим для служебных
     * мероприятий администратора.
     */
    private Long schoolId;

    @PositiveOrZero
    private Integer requestedPointsPerAttendee;

    private Boolean registrationRequired;
}
