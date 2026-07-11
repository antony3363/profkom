package com.example.events_service.service;

import com.example.events_service.dto.SegmentEventCreateRequestDTO;
import com.example.events_service.dto.SegmentEventResponseDTO;
import com.example.events_service.dto.SegmentEventUpdateRequestDTO;
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
public class SegmentEventService {

    private final SegmentEventRepository segmentEventRepository;
    private final EventRepository eventRepository;

    @Transactional
    public SegmentEventResponseDTO createSegmentEvent(SegmentEventCreateRequestDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + dto.getEventId()));

        SegmentEvent segmentEvent = SegmentEvent.builder()
                .event(event)
                .segmentId(dto.getSegmentId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .geoPoint(dto.getGeoPoint())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .pointGain(dto.getPointGain())
                .build();

        segmentEvent = segmentEventRepository.save(segmentEvent);
        event.getSegmentEvents().add(segmentEvent);
        eventRepository.save(event);

        return mapToResponseDTO(segmentEvent);
    }

    @Transactional(readOnly = true)
    public SegmentEventResponseDTO getSegmentEventById(UUID segmentEventId) {
        SegmentEvent segmentEvent = segmentEventRepository.findById(segmentEventId)
                .orElseThrow(() -> new RuntimeException("SegmentEvent not found with id: " + segmentEventId));
        return mapToResponseDTO(segmentEvent);
    }

    @Transactional(readOnly = true)
    public List<SegmentEventResponseDTO> getSegmentEventsByEventId(UUID eventId) {
        List<SegmentEvent> segmentEvents = segmentEventRepository.findByEvent_EventId(eventId);
        return segmentEvents.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SegmentEventResponseDTO updateSegmentEvent(UUID segmentEventId, SegmentEventUpdateRequestDTO dto) {
        SegmentEvent segmentEvent = segmentEventRepository.findById(segmentEventId)
                .orElseThrow(() -> new RuntimeException("SegmentEvent not found with id: " + segmentEventId));

        if (dto.getTitle() != null) segmentEvent.setTitle(dto.getTitle());
        if (dto.getDescription() != null) segmentEvent.setDescription(dto.getDescription());
        if (dto.getShortDescription() != null) segmentEvent.setShortDescription(dto.getShortDescription());
        if (dto.getGeoPoint() != null) segmentEvent.setGeoPoint(dto.getGeoPoint());
        if (dto.getStartAt() != null) segmentEvent.setStartAt(dto.getStartAt());
        if (dto.getEndAt() != null) segmentEvent.setEndAt(dto.getEndAt());
        if (dto.getPointGain() != null) segmentEvent.setPointGain(dto.getPointGain());

        segmentEvent = segmentEventRepository.save(segmentEvent);
        return mapToResponseDTO(segmentEvent);
    }

    @Transactional
    public void deleteSegmentEvent(UUID segmentEventId) {
        SegmentEvent segmentEvent = segmentEventRepository.findById(segmentEventId)
                .orElseThrow(() -> new RuntimeException("SegmentEvent not found with id: " + segmentEventId));
        segmentEventRepository.delete(segmentEvent);
    }

    private SegmentEventResponseDTO mapToResponseDTO(SegmentEvent segmentEvent) {
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
}