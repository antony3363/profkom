package com.example.events_service.repositories;

import com.example.events_service.entities.VolunteerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VolunteerRepository extends JpaRepository<VolunteerRecord, UUID> {
    List<VolunteerRecord> findAllByEvent_EventId(UUID eventId);
    List<VolunteerRecord> findAll();
    boolean existsByEvent_EventIdAndLichnostId(UUID eventId, long lichnostId);
}
