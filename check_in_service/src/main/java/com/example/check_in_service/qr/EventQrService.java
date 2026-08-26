package com.example.check_in_service.qr;

import com.example.check_in_service.exceptions.InvalidQrPayloadException;
import com.example.check_in_service.grpc.EventGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class EventQrService {

    private static final String PREFIX = "event";

    private final EventGrpcClient eventGrpcClient;

    public String generateEventPayload(UUID eventId) {
        boolean registrationRequired = eventGrpcClient.getEventDetails(eventId).registrationRequired();
        return format(eventId, registrationRequired);
    }

    private String format(UUID eventId, boolean registrationRequired) {
        return PREFIX + ":" + eventId + ":" + registrationRequired;
    }

    public EventQrPayload parseEventPayload(String payload) {
        if (payload == null) {
            throw new InvalidQrPayloadException("QR payload is missing");
        }

        String[] parts = payload.split(":");
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new InvalidQrPayloadException("Malformed event QR payload");
        }

        try {
            UUID eventId = UUID.fromString(parts[1]);
            boolean registrationRequired = Boolean.parseBoolean(parts[2]);
            return new EventQrPayload(eventId, registrationRequired);
        } catch (IllegalArgumentException e) {
            throw new InvalidQrPayloadException("Malformed event QR payload");
        }
    }
}
