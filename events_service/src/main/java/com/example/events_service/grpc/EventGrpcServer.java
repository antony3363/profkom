package com.example.events_service.grpc;

import com.example.events_service.entities.Event;
import com.example.events_service.exceptions.EntityNotFoundException;
import com.example.events_service.repositories.EventRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventGrpcServer extends EventGrpcServiceGrpc.EventGrpcServiceImplBase {

    private final EventRepository eventRepository;

    @Override
    public void getEvent(GetEventRequest request, StreamObserver<EventInfo> responseObserver) {
        try {
            UUID eventId = UUID.fromString(request.getEventId());
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

            EventInfo.Builder builder = EventInfo.newBuilder()
                    .setEventId(event.getEventId().toString())
                    .setRegistrationRequired(event.isRegistrationRequired())
                    .setStatus(event.getStatus().name())
                    .setModerationStatus(event.getModerationStatus().name());
            if (event.getPointsPerAttendee() != null) builder.setPointsPerAttendee(event.getPointsPerAttendee());
            if (event.getReviewedBy() != null) builder.setReviewedBy(event.getReviewedBy());
            if (event.getSchoolId() != null) builder.setSchoolId(event.getSchoolId());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (EntityNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }
}
