package com.example.events_service.service;

import com.example.events_service.dto.*;
import com.example.events_service.entity.Event;
import com.example.events_service.entity.SegmentEvent;
import com.example.events_service.repository.EventRepository;
import com.example.events_service.repository.SegmentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SegmentEventRepository segmentEventRepository;

    @Transactional
    public EventResponseDTO createEvent(EventCreateRequestDTO dto) {
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
                .build();

        event = eventRepository.save(event);
        return mapToResponseDTO(event);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return mapToResponseDTO(event);
    }

    @Transactional(readOnly = true)
    public List<EventCatalogDTO> getAllEventsForCatalog() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(this::mapToCatalogDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventShortDescriptionResponse getShortEventDescription(UUID eventId){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return mapToShortDescription(event);
    }

    @Transactional
    public EventResponseDTO updateEvent(UUID eventId, EventUpdateRequestDTO dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getShortDescription() != null) event.setShortDescription(dto.getShortDescription());
        if (dto.getImage() != null) event.setImage(dto.getImage());
        if (dto.getRegistrationStartAt() != null) event.setRegistrationStartAt(dto.getRegistrationStartAt());
        if (dto.getRegistrationEndAt() != null) event.setRegistrationEndAt(dto.getRegistrationEndAt());
        if (dto.getStartAt() != null) event.setStartAt(dto.getStartAt());
        if (dto.getEndAt() != null) event.setEndAt(dto.getEndAt());
        if (dto.getAvailableGroupIds() != null) event.setAvailableGroupIds(dto.getAvailableGroupIds());

        event = eventRepository.save(event);
        return mapToResponseDTO(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }





    private EventResponseDTO mapToResponseDTO(Event event) {
        List<SegmentEventResponseDTO> segmentEvents = event.getSegmentEvents().stream()
                .map(this::mapSegmentEventToResponseDTO)
                .collect(Collectors.toList());

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
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .segmentEvents(segmentEvents)
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
                .build();
    }


    private SegmentEventResponseDTO mapSegmentEventToResponseDTO(SegmentEvent segmentEvent) {
        return SegmentEventResponseDTO.builder()
                .segmentEventId(segmentEvent.getSegmentEventId())
                .eventId(segmentEvent.getEvent().getEventId())
                .segmentId(segmentEvent.getSegmentId())
                .title(segmentEvent.getTitle())
                .description(segmentEvent.getDescription())
                .shortDescription(segmentEvent.getShortDescription())
                .geoPoint(segmentEvent.getGeoPoint())
                .startAt(segmentEvent.getStartAt())
                .endAt(segmentEvent.getEndAt())
                .pointGain(segmentEvent.getPointGain())
                .createdAt(segmentEvent.getCreatedAt())
                .updatedAt(segmentEvent.getUpdatedAt())
                .build();
    }

    private EventShortDescriptionResponse mapToShortDescription(Event event){
        return EventShortDescriptionResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .shortDescription(event.getShortDescription())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .build();

    }
}
