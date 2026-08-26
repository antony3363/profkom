package com.example.check_in_service.repositories;

import com.example.check_in_service.entities.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    List<CheckIn> findByRegistration_RegistrationId(UUID registrationId);
    List<CheckIn> findByEventId(UUID eventId);
    boolean existsByPersonIdAndEventId(long personId, UUID eventId);
}
