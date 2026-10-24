package com.example.events_service.services;

import com.example.events_service.DTOs.*;
import com.example.events_service.entities.Event;
import com.example.events_service.enums.EventModerationStatus;
import com.example.events_service.enums.EventStatus;
import com.example.events_service.exceptions.DuplicateRecordException;
import com.example.events_service.exceptions.EntityNotFoundException;
import com.example.events_service.exceptions.InvalidEventDataException;
import com.example.events_service.exceptions.UnauthorizedException;
import com.example.events_service.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PROFORG_SCHOOL = "PROFORG_SCHOOL";

    private final EventRepository eventRepository;

    @Transactional
    public EventResponseDTO submitEvent(EventCreateRequestDTO dto) {
        validateEventTiming(dto.getStartAt(), dto.getEndAt());

        if (eventRepository.existsDuplicate(dto.getTitle(), dto.getStartAt(), dto.getEndAt(), dto.getSchoolId())) {
            throw new DuplicateRecordException("Event with the same title and time range already exists");
        }

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .image(dto.getImage())
                .location(dto.getLocation())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .availableGroupIds(dto.getAvailableGroupIds())
                .ownerId(dto.getOwnerId())
                .schoolId(dto.getSchoolId())
                .requestedPointsPerAttendee(dto.getRequestedPointsPerAttendee())
                .registrationRequired(dto.isRegistrationRequired())
                .status(EventStatus.DRAFT)
                .moderationStatus(EventModerationStatus.SUBMITTED)
                .build();

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    /**
     * Черновик виден только своему владельцу — для всех остальных eventId ведёт
     * себя так, будто его не существует (не 403 — чтобы не подтверждать сам факт
     * существования чужого черновика).
     */
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(UUID eventId, Long callerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        if (event.getModerationStatus() == EventModerationStatus.DRAFT
                && (callerId == null || event.getOwnerId() != callerId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return mapToResponseDTO(event);
    }

    @Transactional(readOnly = true)
    public List<EventCatalogDTO> getAllEventsForCatalog() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED && event.getSchoolId() != null)
                .map(this::mapToCatalogDTO)
                .collect(Collectors.toList());
    }

    /**
     * Для администратора: вообще без разбора по статусу — черновики профоргов
     * (DRAFT) исключены принудительно, остальное (SUBMITTED/APPROVED/DEFERRED/
     * REJECTED) идёт в одну выборку, разделённую только по времени.
     */
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsForAdmin(boolean past) {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = past
                ? eventRepository.findByEndAtBeforeAndModerationStatusNot(now, EventModerationStatus.DRAFT)
                : eventRepository.findByEndAtGreaterThanEqualAndModerationStatusNot(now, EventModerationStatus.DRAFT);
        return events.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponseDTO saveNewDraft(EventDraftRequestDTO dto, long callerId) {
        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .image(dto.getImage())
                .location(dto.getLocation())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .availableGroupIds(dto.getAvailableGroupIds())
                .ownerId(callerId)
                .schoolId(dto.getSchoolId())
                .requestedPointsPerAttendee(dto.getRequestedPointsPerAttendee())
                .registrationRequired(dto.getRegistrationRequired() != null && dto.getRegistrationRequired())
                .status(EventStatus.DRAFT)
                .moderationStatus(EventModerationStatus.DRAFT)
                .build();
        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getMyDrafts(long callerId) {
        return eventRepository.findByOwnerIdAndModerationStatus(callerId, EventModerationStatus.DRAFT).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Переводит черновик в SUBMITTED — здесь впервые требуется полнота полей,
     * которая не проверялась при сохранении черновика.
     */
    @Transactional
    public EventResponseDTO submitDraft(UUID eventId, long callerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        requireDraftOwner(event, callerId);

        if (event.getModerationStatus() != EventModerationStatus.DRAFT) {
            throw new InvalidEventDataException("moderationStatus", "Event is not a draft");
        }
        requireFilled(event.getTitle(), "title");
        requireFilled(event.getDescription(), "description");
        requireFilled(event.getShortDescription(), "shortDescription");
        if (event.getStartAt() == null) {
            throw new InvalidEventDataException("startAt", "startAt must be set before submitting");
        }
        if (event.getEndAt() == null) {
            throw new InvalidEventDataException("endAt", "endAt must be set before submitting");
        }
        validateEventTiming(event.getStartAt(), event.getEndAt());

        event.setModerationStatus(EventModerationStatus.SUBMITTED);
        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }



    @Transactional(readOnly = true)
    public EventShortDescriptionResponse getShortEventDescription(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        return mapToShortDescription(event);
    }

    /**
     * Единая точка редактирования для черновика и уже отправленной заявки:
     * - черновик (moderationStatus=DRAFT) — редактировать может только владелец
     *   (ownerId), и только пока он черновик — доступны все поля, включая
     *   schoolId/requestedPointsPerAttendee.
     * - отправленная заявка (SUBMITTED и далее) — владелец школы или ADMIN,
     *   как раньше; schoolId/requestedPointsPerAttendee уже зафиксированы и
     *   недоступны для правки.
     * - pointsPerAttendee (фактически назначенные баллы) — только ADMIN, в
     *   любой момент, независимо от статуса (правка человеческой ошибки после
     *   одобрения).
     */
    @Transactional
    public EventResponseDTO updateEvent(UUID eventId, EventUpdateRequestDTO dto, String role, Long callerId, Long callerSchoolId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        boolean isDraft = event.getModerationStatus() == EventModerationStatus.DRAFT;
        boolean isAdmin = ROLE_ADMIN.equals(role);

        if (isDraft) {
            if (callerId == null) {
                throw new UnauthorizedException("Missing X-Lichnost-Id header");
            }
            requireDraftOwner(event, callerId);
        } else {
            requireOwnerOrAdmin(event, role, callerSchoolId);
        }

        if (dto.getTitle() != null) {
            if (dto.getTitle().isBlank()) {
                throw new InvalidEventDataException("title", "Event title must not be blank");
            }
            event.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getShortDescription() != null) event.setShortDescription(dto.getShortDescription());
        if (dto.getImage() != null) event.setImage(dto.getImage());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());

        // Валидируем и пересчитываем пару дат только если клиент реально прислал
        // хоть одно из этих 2 полей — иначе мероприятие, сохранённое ДО того, как
        // появилась эта проверка (и чьи даты ей не удовлетворяют), навсегда
        // заблокировало бы правку любых других, неродственных полей.
        boolean timingFieldProvided = dto.getStartAt() != null || dto.getEndAt() != null;
        if (timingFieldProvided) {
            LocalDateTime startAt = dto.getStartAt() != null ? dto.getStartAt() : event.getStartAt();
            LocalDateTime endAt = dto.getEndAt() != null ? dto.getEndAt() : event.getEndAt();
            validateEventTiming(startAt, endAt);
            event.setStartAt(startAt);
            event.setEndAt(endAt);
        }

        if (dto.getAvailableGroupIds() != null) event.setAvailableGroupIds(dto.getAvailableGroupIds());
        if (dto.getRegistrationRequired() != null) event.setRegistrationRequired(dto.getRegistrationRequired());
        if (dto.getStatus() != null) event.setStatus(dto.getStatus());

        if (dto.getSchoolId() != null || dto.getRequestedPointsPerAttendee() != null) {
            if (!isDraft) {
                throw new InvalidEventDataException("schoolId",
                        "schoolId/requestedPointsPerAttendee can only be changed while the event is still a draft");
            }
            if (dto.getSchoolId() != null) event.setSchoolId(dto.getSchoolId());
            if (dto.getRequestedPointsPerAttendee() != null) event.setRequestedPointsPerAttendee(dto.getRequestedPointsPerAttendee());
        }

        if (dto.getPointsPerAttendee() != null) {
            if (!isAdmin) {
                throw new UnauthorizedException("Менять фактически назначенные баллы может только администратор");
            }
            event.setPointsPerAttendee(dto.getPointsPerAttendee());
        }

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO acceptEvent(UUID eventId, EventModerationDecisionDTO decision, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        // Принятие заявки публикует мероприятие — не даём опубликовать то, чьи даты
        // не проходят проверку (актуально для заявок, поданных до того, как появилась
        // эта проверка). Исправляется через PUT с корректными датами.
        validateEventTiming(event.getStartAt(), event.getEndAt());

        if (decision.getPointsPerAttendee() != null) {
            event.setPointsPerAttendee(decision.getPointsPerAttendee());
        }
        event.setModerationStatus(EventModerationStatus.APPROVED);
        event.setStatus(EventStatus.PUBLISHED);
        event.setReviewedBy(reviewerId);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO deferEvent(UUID eventId, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        event.setModerationStatus(EventModerationStatus.DEFERRED);
        event.setReviewedBy(reviewerId);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO rejectEvent(UUID eventId, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        event.setModerationStatus(EventModerationStatus.REJECTED);
        event.setStatus(EventStatus.CANCELLED);
        event.setReviewedBy(reviewerId);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId, String role, Long callerSchoolId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        requireOwnerOrAdmin(event, role, callerSchoolId);
        eventRepository.deleteById(eventId);
    }

    private void requireOwnerOrAdmin(Event event, String role, Long callerSchoolId) {
        if (ROLE_ADMIN.equals(role)) {
            return;
        }
        if (ROLE_PROFORG_SCHOOL.equals(role) && callerSchoolId != null && callerSchoolId.equals(event.getSchoolId())) {
            return;
        }
        throw new UnauthorizedException("Редактировать мероприятие может только профорг его школы или администратор");
    }

    private void requireFilled(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidEventDataException(field, field + " must be filled before submitting");
        }
    }

    private void requireDraftOwner(Event event, long callerId) {
        if (event.getOwnerId() != callerId) {
            throw new UnauthorizedException("Черновик доступен только тому, кто его создал");
        }
    }

    /**
     * Покрывает Б-7 из багрепорта по EventController. Б-5/Б-6/Б-8 (цепочка с датами
     * регистрации) больше не применимы — поля регистрации убраны из Event.
     */
    private void validateEventTiming(LocalDateTime startAt, LocalDateTime endAt) {
        if (!startAt.isBefore(endAt)) {
            throw new InvalidEventDataException("startAt", "startAt must be before endAt");
        }
    }

    private EventResponseDTO mapToResponseDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .shortDescription(event.getShortDescription())
                .image(event.getImage())
                .location(event.getLocation())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .availableGroupIds(event.getAvailableGroupIds())
                .ownerId(event.getOwnerId())
                .schoolId(event.getSchoolId())
                .status(event.getStatus())
                .moderationStatus(event.getModerationStatus())
                .requestedPointsPerAttendee(event.getRequestedPointsPerAttendee())
                .pointsPerAttendee(event.getPointsPerAttendee())
                .reviewedBy(event.getReviewedBy())
                .reviewedAt(event.getReviewedAt())
                .registrationRequired(event.isRegistrationRequired())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private EventCatalogDTO mapToCatalogDTO(Event event) {
        return EventCatalogDTO.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .shortDescription(event.getShortDescription())
                .image(event.getImage())
                .location(event.getLocation())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .pointsPerAttendee(event.getPointsPerAttendee())
                .build();
    }

    private EventShortDescriptionResponse mapToShortDescription(Event event) {
        return EventShortDescriptionResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .shortDescription(event.getShortDescription())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .build();
    }
}
