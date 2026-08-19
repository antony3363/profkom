package com.example.events_service.controllers;

import com.example.events_service.DTOs.*;
import com.example.events_service.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateRequestDTO dto) {
        EventResponseDTO response = eventService.createEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable UUID eventId) {
        EventResponseDTO response = eventService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EventCatalogDTO>> getAllEvents() {
        List<EventCatalogDTO> events = eventService.getAllEventsForCatalog();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody EventUpdateRequestDTO dto) {
        EventResponseDTO response = eventService.updateEvent(eventId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/short/{eventId}")
    public ResponseEntity<EventShortDescriptionResponse> getShortEvent(@PathVariable UUID eventId) {
        EventShortDescriptionResponse response = eventService.getShortEventDescription(eventId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
