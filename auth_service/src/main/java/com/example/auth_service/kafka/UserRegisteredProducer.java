package com.example.auth_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredProducer {

    private static final String TOPIC = "user-registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publish(UserRegisteredEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.lichnostId()), event);
    }
}
