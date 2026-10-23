package com.example.events_service.repositories;

import com.example.events_service.entities.Event;
import com.example.events_service.enums.EventModerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByEventId(UUID eventId);

    /**
     * Для списка администратора: без разбора по status/moderationStatus (включая
     * неподтверждённые) — единственный критерий, время. Черновики (DRAFT) явно
     * исключены — админ их не должен видеть вообще. Сортировка — на фронте.
     */
    List<Event> findByEndAtBeforeAndModerationStatusNot(LocalDateTime cutoff, EventModerationStatus excludedStatus);

    List<Event> findByEndAtGreaterThanEqualAndModerationStatusNot(LocalDateTime cutoff, EventModerationStatus excludedStatus);

    List<Event> findByOwnerIdAndModerationStatus(long ownerId, EventModerationStatus moderationStatus);

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
