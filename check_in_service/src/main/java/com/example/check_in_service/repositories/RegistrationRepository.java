package com.example.check_in_service.repositories;

import com.example.check_in_service.entities.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByEventId(UUID eventId);
    List<Registration> findByPersonId(long personId);
    boolean existsByPersonIdAndEventId(long personId, UUID eventId);
    Optional<Registration> findByPersonIdAndEventId(long personId, UUID eventId);
}
