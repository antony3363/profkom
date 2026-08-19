package com.example.check_in_service.qr;

import java.util.UUID;

public record SegmentQrPayload(UUID eventId, UUID segmentId, boolean registrationRequired) {
}
