package com.example.auth_service.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * ЗАГЛУШКА обмена с SSO ТПУ: в целевой архитектуре сюда приходит authorization code
 * от ТПУ (OAuth2/OIDC, Authorization Code + PKCE), а personId извлекается из
 * id-токена. Реального доступа к OIDC-эндпоинту ТПУ нет, поэтому personId передаётся
 * напрямую — заменить на реальный обмен кода при подключении.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotNull
    private Long personId;
}
