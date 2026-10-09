package com.example.events_service.services;

import com.example.events_service.DTOs.*;
import com.example.events_service.entities.Event;
import com.example.events_service.enums.EventModerationStatus;
import com.example.events_service.enums.EventStatus;
import com.example.events_service.exceptions.DuplicateRecordException;
import com.example.events_service.exceptions.EntityNotFoundException;
import com.example.events_service.exceptions.InvalidEventDataException;
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

    private final EventRepository eventRepository;

    @Transactional
    public EventResponseDTO submitEvent(EventCreateRequestDTO dto) {
        validateEventTiming(dto.getRegistrationStartAt(), dto.getRegistrationEndAt(), dto.getStartAt(), dto.getEndAt());

        if (eventRepository.existsDuplicate(dto.getTitle(), dto.getStartAt(), dto.getEndAt(), dto.getSchoolId())) {
            throw new DuplicateRecordException("Event with the same title and time range already exists");
        }

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .image(dto.getImage())
                .registrationStartAt(dto.getRegistrationStartAt())
                .registrationEndAt(dto.getRegistrationEndAt())
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

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
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

    @Transactional(readOnly = true)
    public EventShortDescriptionResponse getShortEventDescription(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        return mapToShortDescription(event);
    }

    @Transactional
    public EventResponseDTO updateEvent(UUID eventId, EventUpdateRequestDTO dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (dto.getTitle() != null) {
            if (dto.getTitle().isBlank()) {
                throw new InvalidEventDataException("title", "Event title must not be blank");
            }
            event.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getShortDescription() != null) event.setShortDescription(dto.getShortDescription());
        if (dto.getImage() != null) event.setImage(dto.getImage());

        // Валидируем и пересчитываем цепочку дат только если клиент реально прислал
        // хоть одно из этих 4 полей — иначе мероприятие, сохранённое ДО того, как
        // появилась эта проверка (и чьи даты ей не удовлетворяют), навсегда
        // заблокировало бы правку любых других, неродственных полей.
        boolean timingFieldProvided = dto.getRegistrationStartAt() != null || dto.getRegistrationEndAt() != null
                || dto.getStartAt() != null || dto.getEndAt() != null;
        if (timingFieldProvided) {
            LocalDateTime registrationStartAt = dto.getRegistrationStartAt() != null
                    ? dto.getRegistrationStartAt() : event.getRegistrationStartAt();
            LocalDateTime registrationEndAt = dto.getRegistrationEndAt() != null
                    ? dto.getRegistrationEndAt() : event.getRegistrationEndAt();
            LocalDateTime startAt = dto.getStartAt() != null ? dto.getStartAt() : event.getStartAt();
            LocalDateTime endAt = dto.getEndAt() != null ? dto.getEndAt() : event.getEndAt();
            validateEventTiming(registrationStartAt, registrationEndAt, startAt, endAt);
            event.setRegistrationStartAt(registrationStartAt);
            event.setRegistrationEndAt(registrationEndAt);
            event.setStartAt(startAt);
            event.setEndAt(endAt);
        }

        if (dto.getAvailableGroupIds() != null) event.setAvailableGroupIds(dto.getAvailableGroupIds());
        if (dto.getRegistrationRequired() != null) event.setRegistrationRequired(dto.getRegistrationRequired());
        if (dto.getStatus() != null) event.setStatus(dto.getStatus());

        event = eventRepository.saveAndFlush(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO acceptEvent(UUID eventId, EventModerationDecisionDTO decision, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        // Принятие заявки публикует мероприятие — не даём опубликовать то, чьи даты
        // не проходят текущую цепочку (актуально для заявок, поданных до того, как
        // появилась эта проверка). Исправляется через PUT с корректными датами.
        validateEventTiming(event.getRegistrationStartAt(), event.getRegistrationEndAt(),
                event.getStartAt(), event.getEndAt());

        event.setPointsPerAttendee(decision.getPointsPerAttendee());
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
    public void deleteEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    /**
     * Цепочка должна соблюдаться целиком: начало регистрации < конец регистрации
     * <= начало мероприятия < конец мероприятия. Покрывает Б-5/Б-6 (регистрация),
     * Б-7 (само мероприятие) и Б-8 (регистрация не может закрываться позже начала
     * мероприятия) из багрепорта по EventController.
     */
    private void validateEventTiming(LocalDateTime registrationStartAt, LocalDateTime registrationEndAt,
                                      LocalDateTime startAt, LocalDateTime endAt) {
        if (!registrationStartAt.isBefore(registrationEndAt)) {
            throw new InvalidEventDataException("registrationStartAt", "registrationStartAt must be before registrationEndAt");
        }
        if (registrationEndAt.isAfter(startAt)) {
            throw new InvalidEventDataException("registrationEndAt", "registrationEndAt must not be after startAt");
        }
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
                .registrationStartAt(event.getRegistrationStartAt())
                .registrationEndAt(event.getRegistrationEndAt())
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
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .registrationEndAt(event.getRegistrationEndAt())
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
