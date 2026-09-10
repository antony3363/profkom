package com.example.auth_service.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * ЗАГЛУШКА обмена с SSO ТПУ: в целевой архитектуре сюда приходит authorization code
 * от ТПУ (OAuth2/OIDC, Authorization Code + PKCE), а lichnostId/email/firstName/
 * lastName извлекаются из id-токена. Реального доступа к OIDC-эндпоинту ТПУ нет,
 * поэтому они передаются напрямую — заменить на реальный обмен кода при подключении.
 * email/firstName/lastName нужны только при первом входе (публикуются в
 * UserRegistered для Profile Service) — необязательны, чтобы не ломать вызовы,
 * где их взять неоткуда.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotNull
    private Long lichnostId;

    private String email;
    private String firstName;
    private String lastName;
}
