package com.example.events_service.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequestDTO {

    @NotBlank
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String shortDescription;

    private String image;

    @NotNull
    private LocalDateTime registrationStartAt;

    @NotNull
    private LocalDateTime registrationEndAt;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private List<String> availableGroupIds;

    @NotNull
    private long ownerId;

    /**
     * Школа, от лица которой подаётся заявка. Null допустим только для служебных
     * мероприятий, создаваемых администратором (не попадают в каталог).
     */
    private Long schoolId;

    /**
     * Сколько баллов за посещение запрашивает профорг школы — окончательное число
     * назначает Литвинов при принятии заявки.
     */
    @PositiveOrZero
    private Integer requestedPointsPerAttendee;

    private boolean registrationRequired;
}
