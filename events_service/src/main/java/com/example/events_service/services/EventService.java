package com.example.events_service.services;

import com.example.events_service.DTOs.*;
import com.example.events_service.entities.Event;
import com.example.events_service.enums.EventModerationStatus;
import com.example.events_service.enums.EventStatus;
import com.example.events_service.exceptions.EntityNotFoundException;
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

        event = eventRepository.save(event);
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

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getShortDescription() != null) event.setShortDescription(dto.getShortDescription());
        if (dto.getImage() != null) event.setImage(dto.getImage());
        if (dto.getRegistrationStartAt() != null) event.setRegistrationStartAt(dto.getRegistrationStartAt());
        if (dto.getRegistrationEndAt() != null) event.setRegistrationEndAt(dto.getRegistrationEndAt());
        if (dto.getStartAt() != null) event.setStartAt(dto.getStartAt());
        if (dto.getEndAt() != null) event.setEndAt(dto.getEndAt());
        if (dto.getAvailableGroupIds() != null) event.setAvailableGroupIds(dto.getAvailableGroupIds());
        if (dto.getRegistrationRequired() != null) event.setRegistrationRequired(dto.getRegistrationRequired());
        if (dto.getStatus() != null) event.setStatus(dto.getStatus());

        event = eventRepository.save(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO acceptEvent(UUID eventId, EventModerationDecisionDTO decision, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        event.setPointsPerAttendee(decision.getPointsPerAttendee());
        event.setModerationStatus(EventModerationStatus.APPROVED);
        event.setStatus(EventStatus.PUBLISHED);
        event.setReviewedBy(reviewerId);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.save(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO deferEvent(UUID eventId, long reviewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        event.setModerationStatus(EventModerationStatus.DEFERRED);
        event.setReviewedBy(reviewerId);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.save(event);
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

        event = eventRepository.save(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        eventRepository.deleteById(eventId);
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
