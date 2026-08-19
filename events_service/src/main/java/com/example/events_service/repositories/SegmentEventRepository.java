package com.example.events_service.repositories;

import com.example.events_service.entities.SegmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SegmentEventRepository extends JpaRepository<SegmentEvent, UUID> {
    List<SegmentEvent> findByEvent_EventIdOrderByOrderIndexAsc(UUID eventId);
}
