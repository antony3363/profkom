package com.example.check_in_service.DTOs;

import com.example.check_in_service.enums.CheckInType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInCreateRequestDTO {

    @NotNull
    private CheckInType type;

    /**
     * SELF_SCAN: the event QR payload the user scanned.
     * STAFF_SCAN: the person QR payload the volunteer scanned.
     */
    @NotNull
    private String qrPayload;

    /**
     * Required only for STAFF_SCAN: the event the volunteer's station is checking people into.
     */
    private UUID eventId;
}
