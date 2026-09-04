package com.example.profile_service.kafka;

import com.example.profile_service.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "user-registered")
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.email() == null || event.firstName() == null || event.lastName() == null) {
            return; // заглушка SSO не прислала данные — профиль создадут вручную позже (POST /profiles)
        }
        userProfileService.createFromRegistration(event.personId(), event.email(), event.firstName(), event.lastName());
    }
}
