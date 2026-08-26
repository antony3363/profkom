package com.example.profile_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id", nullable = false, updatable = false)
    private Long schoolId;

    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Профорг школы — единственный уровень профоргов с реальными правами в системе
     * (переводы со счёта школы, заявки на мероприятия). Не FK-объект, а просто id
     * person_id из Profile Service же — держим plain-полем, чтобы не тянуть
     * двунаправленный граф сущностей ради поля, которое нужно только для чтения.
     */
    @Column(name = "proforg_id")
    private Long proforgId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
