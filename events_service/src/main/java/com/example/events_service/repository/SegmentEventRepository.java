package com.example.events_service.repository;

import com.example.events_service.entity.SegmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SegmentEventRepository extends JpaRepository<SegmentEvent, UUID> {
    List<SegmentEvent> findByEvent_EventId(UUID eventId);
}
