package com.example.events_service.controller;


import com.example.events_service.dto.SegmentEventCreateRequestDTO;
import com.example.events_service.dto.SegmentEventResponseDTO;
import com.example.events_service.dto.SegmentEventUpdateRequestDTO;
import com.example.events_service.service.SegmentEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/segment-events")
@RequiredArgsConstructor
public class SegmentEventController {

    private final SegmentEventService segmentEventService;


    @PostMapping
    public ResponseEntity<SegmentEventResponseDTO> createSegmentEvent(@RequestBody SegmentEventCreateRequestDTO dto) {
        SegmentEventResponseDTO response = segmentEventService.createSegmentEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{segmentEventId}")
    public ResponseEntity<SegmentEventResponseDTO> getSegmentEvent(@PathVariable UUID segmentEventId) {
        SegmentEventResponseDTO response = segmentEventService.getSegmentEventById(segmentEventId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<SegmentEventResponseDTO>> getSegmentEvents(
            @RequestParam(required = false) UUID eventId) {
        List<SegmentEventResponseDTO> segmentEvents = (eventId != null)
                ? segmentEventService.getSegmentEventsByEventId(eventId)
                : List.of();
        return ResponseEntity.ok(segmentEvents);
    }


    @PutMapping("/{segmentEventId}")
    public ResponseEntity<SegmentEventResponseDTO> updateSegmentEvent(
            @PathVariable UUID segmentEventId,
            @RequestBody SegmentEventUpdateRequestDTO dto) {
        SegmentEventResponseDTO response = segmentEventService.updateSegmentEvent(segmentEventId, dto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{segmentEventId}")
    public ResponseEntity<Void> deleteSegmentEvent(@PathVariable UUID segmentEventId) {
        segmentEventService.deleteSegmentEvent(segmentEventId);
        return ResponseEntity.noContent().build();
    }

}
