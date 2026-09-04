package com.example.profile_service.kafka;

/**
 * Публикуется Auth Service при создании нового AppUser (первый вход). email/
 * firstName/lastName nullable — заглушка SSO ТПУ не всегда их присылает.
 * Profile Service создаёт профиль по этому событию, только если все три поля
 * присутствуют — иначе нечем заполнить обязательные поля UserProfile.
 */
public record UserRegisteredEvent(long personId, String email, String firstName, String lastName) {
}
