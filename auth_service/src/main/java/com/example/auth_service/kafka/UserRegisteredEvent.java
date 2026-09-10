package com.example.auth_service.kafka;

/**
 * Публикуется Auth Service при создании нового AppUser (первый вход). email/
 * firstName/lastName берутся из тела /auth/login — заглушка SSO ТПУ пока не
 * обязана их присылать, поэтому они nullable; когда реальный SSO подключится,
 * это будут claims из id-токена. Profile Service создаёт профиль по этому
 * событию, только если все три поля присутствуют.
 */
public record UserRegisteredEvent(long lichnostId, String email, String firstName, String lastName) {
}
