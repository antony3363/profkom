package com.example.check_in_service.qr;

import java.util.UUID;

public record EventQrPayload(UUID eventId, boolean registrationRequired) {
}
