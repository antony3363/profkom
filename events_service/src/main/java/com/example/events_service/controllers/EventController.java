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

    /**
     * Черновик (moderationStatus=DRAFT) виден только своему владельцу — для всех
     * остальных событий доступ открытый, как раньше.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @PathVariable UUID eventId) {
        EventResponseDTO response = eventService.getEventById(eventId, callerId);
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

    /**
     * Полный список мероприятий/заявок для администратора — без разбора по статусу
     * (черновики профоргов исключены принудительно, см. EventService), единственный
     * фильтр — прошедшие/не прошедшие. Сортировка — на фронте.
     */
    @GetMapping("/moderation")
    public ResponseEntity<List<EventResponseDTO>> getEventsForAdmin(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false, defaultValue = "false") boolean past) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Список мероприятий доступен только администратору");
        }
        return ResponseEntity.ok(eventService.getEventsForAdmin(past));
    }

    /**
     * Сохранить черновик заявки — почти без обязательных полей, владелец берётся
     * из токена (X-Lichnost-Id), а не из тела запроса.
     */
    @PostMapping("/draft")
    public ResponseEntity<EventResponseDTO> saveDraft(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @Valid @RequestBody EventDraftRequestDTO dto) {
        if (callerId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        if (!ROLE_PROFORG_SCHOOL.equals(role) && !ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Черновик может создать только профорг школы или администратор");
        }
        EventResponseDTO response = eventService.saveNewDraft(dto, callerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Дозаполнить/изменить ранее сохранённый черновик — только его владелец.
     */
    @PutMapping("/draft/{eventId}")
    public ResponseEntity<EventResponseDTO> updateDraft(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @PathVariable UUID eventId,
            @RequestBody EventDraftRequestDTO dto) {
        if (callerId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        EventResponseDTO response = eventService.updateDraft(eventId, dto, callerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Черновики текущего пользователя — чтобы вернуться и продолжить заполнение.
     */
    @GetMapping("/my-drafts")
    public ResponseEntity<List<EventResponseDTO>> getMyDrafts(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId) {
        if (callerId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        return ResponseEntity.ok(eventService.getMyDrafts(callerId));
    }

    /**
     * Отправить черновик на рассмотрение — здесь впервые проверяется полнота
     * всех обязательных полей (при сохранении черновика они не требовались).
     */
    @PostMapping("/{eventId}/submit")
    public ResponseEntity<EventResponseDTO> submitDraft(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @PathVariable UUID eventId) {
        if (callerId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        EventResponseDTO response = eventService.submitDraft(eventId, callerId);
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
