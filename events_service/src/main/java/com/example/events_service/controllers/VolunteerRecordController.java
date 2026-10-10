package com.example.events_service.controllers;


import com.example.events_service.DTOs.VolunteerRecordCreateRequestDTO;
import com.example.events_service.DTOs.VolunteerRecordResponseDTO;
import com.example.events_service.services.VolunteerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/volunteer_records")
@RequiredArgsConstructor
public class VolunteerRecordController {

    private final VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<VolunteerRecordResponseDTO> createVolunteerRecord(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @Valid @RequestBody VolunteerRecordCreateRequestDTO request) {
        VolunteerRecordResponseDTO response = volunteerService.createVolunteer(request, role, callerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{volunteerEntryId}")
    public ResponseEntity<VolunteerRecordResponseDTO> getVolunteer(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable UUID volunteerEntryId) {
        VolunteerRecordResponseDTO response = volunteerService.getVolunteerById(volunteerEntryId, role, callerId, callerSchoolId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VolunteerRecordResponseDTO>> getVolunteers(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @RequestParam(required = false) UUID eventId) {
        List<VolunteerRecordResponseDTO> volunteers = (eventId != null)
                ? volunteerService.getVolunteersByEventId(eventId, role, callerSchoolId)
                : volunteerService.getAllVolunteers(role);
        return ResponseEntity.ok(volunteers);
    }

    @DeleteMapping("/{volunteerEntryId}")
    public ResponseEntity<Void> deleteVolunteer(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @PathVariable UUID volunteerEntryId) {
        volunteerService.deleteVolunteer(volunteerEntryId, role, callerId);
        return ResponseEntity.noContent().build();
    }

}
