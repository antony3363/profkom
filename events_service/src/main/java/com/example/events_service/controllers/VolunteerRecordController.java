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
    public ResponseEntity<VolunteerRecordResponseDTO> createVolunteerRecord(@Valid @RequestBody VolunteerRecordCreateRequestDTO request) {
        VolunteerRecordResponseDTO response = volunteerService.createVolunteer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{volunteerEntryId}")
    public ResponseEntity<VolunteerRecordResponseDTO> getVolunteer(@PathVariable UUID volunteerEntryId) {
        VolunteerRecordResponseDTO response = volunteerService.getVolunteerById(volunteerEntryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VolunteerRecordResponseDTO>> getVolunteers(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) String faculty) {
        List<VolunteerRecordResponseDTO> volunteers = (eventId != null)
                ? volunteerService.getVolunteersByEventId(eventId, faculty)
                : volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    @DeleteMapping("/{volunteerEntryId}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable UUID volunteerEntryId) {
        volunteerService.deleteVolunteer(volunteerEntryId);
        return ResponseEntity.noContent().build();
    }

}
