package com.example.check_in_service.qr;

import com.example.check_in_service.exceptions.EntityNotFoundException;
import com.example.check_in_service.exceptions.InvalidQrPayloadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SegmentQrService {

    private static final String PREFIX = "segment";

    private final RestClient eventsServiceRestClient;

    public String generateSegmentPayload(UUID segmentId) {
        SegmentQrPayload segment = resolveSegment(segmentId);
        return format(segment);
    }

    private String format(SegmentQrPayload segment) {
        return PREFIX + ":" + segment.eventId() + ":" + segment.segmentId() + ":" + segment.registrationRequired();
    }

    public SegmentQrPayload resolveSegment(UUID segmentId) {
        SegmentLookup segment = fetch("/api/v1/segment-events/{id}", segmentId, SegmentLookup.class,
                "Segment not found with id: " + segmentId);

        EventLookup event = fetch("/api/v1/events/{id}", segment.eventId(), EventLookup.class,
                "Event not found with id: " + segment.eventId());

        return new SegmentQrPayload(segment.eventId(), segmentId, event.registrationRequired());
    }

    public SegmentQrPayload parseSegmentPayload(String payload) {
        if (payload == null) {
            throw new InvalidQrPayloadException("QR payload is missing");
        }

        String[] parts = payload.split(":");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            throw new InvalidQrPayloadException("Malformed segment QR payload");
        }

        try {
            UUID eventId = UUID.fromString(parts[1]);
            UUID segmentId = UUID.fromString(parts[2]);
            boolean registrationRequired = Boolean.parseBoolean(parts[3]);
            return new SegmentQrPayload(eventId, segmentId, registrationRequired);
        } catch (IllegalArgumentException e) {
            throw new InvalidQrPayloadException("Malformed segment QR payload");
        }
    }



    private <T> T fetch(String uri, UUID id, Class<T> type, String notFoundMessage) {
        try {
            T result = eventsServiceRestClient.get()
                    .uri(uri, id)
                    .retrieve()
                    .body(type);
            if (result == null) {
                throw new EntityNotFoundException(notFoundMessage);
            }
            return result;
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException(notFoundMessage);
        }
    }

    private record SegmentLookup(UUID eventId) {
    }

    private record EventLookup(boolean registrationRequired) {
    }
}
