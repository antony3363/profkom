package com.example.check_in_service.grpc;

import com.example.check_in_service.exceptions.EntityNotFoundException;
import com.example.events_service.grpc.EventGrpcServiceGrpc;
import com.example.events_service.grpc.EventInfo;
import com.example.events_service.grpc.GetEventRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventGrpcClient {

    private final EventGrpcServiceGrpc.EventGrpcServiceBlockingStub eventGrpcServiceBlockingStub;

    public EventDetails getEventDetails(UUID eventId) {
        try {
            EventInfo info = eventGrpcServiceBlockingStub.getEvent(
                    GetEventRequest.newBuilder().setEventId(eventId.toString()).build());

            return new EventDetails(
                    eventId,
                    info.getRegistrationRequired(),
                    info.hasPointsPerAttendee() ? info.getPointsPerAttendee() : null,
                    info.hasReviewedBy() ? info.getReviewedBy() : null);
        } catch (StatusRuntimeException e) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
    }
}
