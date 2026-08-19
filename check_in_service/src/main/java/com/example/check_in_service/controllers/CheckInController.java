package com.example.check_in_service.controllers;

import com.example.check_in_service.DTOs.CheckInCreateRequestDTO;
import com.example.check_in_service.DTOs.CheckInResponseDTO;
import com.example.check_in_service.services.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CheckInResponseDTO> createCheckIn(
            @RequestHeader(value = "X-Person-Id", required = false) Long callerPersonId,
            @Valid @RequestBody CheckInCreateRequestDTO dto) {
        CheckInResponseDTO response = checkInService.createCheckIn(dto, callerPersonId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{checkInId}")
    public ResponseEntity<CheckInResponseDTO> getCheckIn(@PathVariable UUID checkInId) {
        CheckInResponseDTO response = checkInService.getCheckInById(checkInId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CheckInResponseDTO>> getCheckIns(
            @RequestParam(required = false) UUID registrationId,
            @RequestParam(required = false) UUID segmentId) {
        List<CheckInResponseDTO> checkIns;
        if (registrationId != null) {
            checkIns = checkInService.getCheckInsByRegistrationId(registrationId);
        } else if (segmentId != null) {
            checkIns = checkInService.getCheckInsBySegmentId(segmentId);
        } else {
            checkIns = checkInService.getAllCheckIns();
        }
        return ResponseEntity.ok(checkIns);
    }

    @DeleteMapping("/{checkInId}")
    public ResponseEntity<Void> deleteCheckIn(@PathVariable UUID checkInId) {
        checkInService.deleteCheckIn(checkInId);
        return ResponseEntity.noContent().build();
    }
}
