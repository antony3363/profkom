package com.example.auth_service.DTOs;

import com.example.auth_service.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * В целевой архитектуре роль PROFORG_SCHOOL синхронизируется автоматически по
 * Kafka-событию при смене School.proforgId в Profile Service — пока эта интеграция
 * не подключена, роль меняется вручную администратором через этот эндпоинт.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequestDTO {

    @NotNull
    private UserRole role;
}
