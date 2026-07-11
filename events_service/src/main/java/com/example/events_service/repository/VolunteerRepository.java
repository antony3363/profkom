package com.example.events_service.repository;

import com.example.events_service.entity.VolunteerRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface VolunteerRepository extends CrudRepository<VolunteerRecord, UUID> {
    List<VolunteerRecord> findAllByEvent_EventId(UUID eventId);
    List<VolunteerRecord> findAll();
}
