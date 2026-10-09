package com.example.events_service.repositories;

import com.example.events_service.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByEventId(UUID eventId);

    /**
     * schoolId null-safe: два служебных мероприятия (schoolId=null) НЕ считаются
     * дубликатом друг друга — обычное равенство (`e.schoolId = :schoolId`) в SQL
     * никогда не истинно для NULL, поэтому сравниваем явно через IS NULL.
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.title = :title AND e.startAt = :startAt AND e.endAt = :endAt "
            + "AND ((:schoolId IS NULL AND e.schoolId IS NULL) OR e.schoolId = :schoolId)")
    boolean existsDuplicate(@Param("title") String title, @Param("startAt") LocalDateTime startAt,
                             @Param("endAt") LocalDateTime endAt, @Param("schoolId") Long schoolId);
}
