package com.example.events_service.controller;


import com.example.events_service.dto.VolunteerRecordCreateRequestDTO;
import com.example.events_service.dto.VolunteerRecordResponseDTO;
import com.example.events_service.dto.VolunteerRecordUpdateRequestDTO;
import com.example.events_service.service.VolunteerService;
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
    public ResponseEntity<VolunteerRecordResponseDTO> createVolunteerRecord(@RequestBody VolunteerRecordCreateRequestDTO request) {
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
            @RequestParam(required = false) UUID eventId) {
        List<VolunteerRecordResponseDTO> volunteers = (eventId != null)
                ? volunteerService.getVolunteersByEventId(eventId)
                : volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    @PutMapping("/{volunteerEntryId}")
    public ResponseEntity<VolunteerRecordResponseDTO> updateVolunteer(
            @PathVariable UUID volunteerEntryId,
            @RequestBody VolunteerRecordUpdateRequestDTO request) {
        VolunteerRecordResponseDTO response = volunteerService.updateVolunteer(volunteerEntryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{volunteerEntryId}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable UUID volunteerEntryId) {
        volunteerService.deleteVolunteer(volunteerEntryId);
        return ResponseEntity.noContent().build();
    }

}
