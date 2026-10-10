package com.example.events_service.controllers;

import com.example.events_service.DTOs.*;
import com.example.events_service.exceptions.UnauthorizedException;
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

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PROFORG_SCHOOL = "PROFORG_SCHOOL";

    private final EventService eventService;

    /**
     * Заявка на мероприятие. Доступно профоргу школы (только для своей школы, из
     * X-School-Id) или администратору (может указать любую школу либо null для
     * служебного мероприятия, скрытого из каталога).
     */
    @PostMapping
    public ResponseEntity<EventResponseDTO> submitEvent(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @Valid @RequestBody EventCreateRequestDTO dto) {
        if (ROLE_PROFORG_SCHOOL.equals(role)) {
            if (callerSchoolId == null || !callerSchoolId.equals(dto.getSchoolId())) {
                throw new UnauthorizedException("Профорг школы может подавать заявки только от своей школы");
            }
        } else if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только профорг школы или администратор может создавать мероприятия");
        }

        EventResponseDTO response = eventService.submitEvent(dto);
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
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable UUID eventId,
            @Valid @RequestBody EventUpdateRequestDTO dto) {
        EventResponseDTO response = eventService.updateEvent(eventId, dto, role, callerSchoolId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/short/{eventId}")
    public ResponseEntity<EventShortDescriptionResponse> getShortEvent(@PathVariable UUID eventId) {
        EventShortDescriptionResponse response = eventService.getShortEventDescription(eventId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/accept")
    public ResponseEntity<EventResponseDTO> acceptEvent(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long reviewerId,
            @Valid @RequestBody EventModerationDecisionDTO decision) {
        requireAdmin(role, reviewerId);
        EventResponseDTO response = eventService.acceptEvent(eventId, decision, reviewerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/defer")
    public ResponseEntity<EventResponseDTO> deferEvent(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long reviewerId) {
        requireAdmin(role, reviewerId);
        EventResponseDTO response = eventService.deferEvent(eventId, reviewerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/reject")
    public ResponseEntity<EventResponseDTO> rejectEvent(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long reviewerId) {
        requireAdmin(role, reviewerId);
        EventResponseDTO response = eventService.rejectEvent(eventId, reviewerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable UUID eventId) {
        eventService.deleteEvent(eventId, role, callerSchoolId);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role, Long callerId) {
        if (!ROLE_ADMIN.equals(role) || callerId == null) {
            throw new UnauthorizedException("Решения по заявкам принимает только администратор");
        }
    }
}
