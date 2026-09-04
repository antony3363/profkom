package com.example.auth_service.kafka;

import com.example.auth_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolProforgChangedConsumer {

    private final UserService userService;

    @KafkaListener(topics = "school-proforg-changed")
    public void onSchoolProforgChanged(SchoolProforgChangedEvent event) {
        userService.applySchoolProforgChange(event.schoolId(), event.oldProforgId(), event.newProforgId());
    }
}
