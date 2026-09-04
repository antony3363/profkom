package com.example.profile_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolProforgChangedProducer {

    private static final String TOPIC = "school-proforg-changed";

    private final KafkaTemplate<String, SchoolProforgChangedEvent> kafkaTemplate;

    public void publish(SchoolProforgChangedEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.schoolId()), event);
    }
}
