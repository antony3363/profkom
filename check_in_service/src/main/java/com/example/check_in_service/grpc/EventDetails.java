package com.example.check_in_service.grpc;

import java.util.UUID;

public record EventDetails(UUID eventId, boolean registrationRequired, Long pointsPerAttendee, Long reviewedBy) {
}
