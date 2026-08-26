package com.example.profile_service.grpc;

import com.example.profile_service.entities.UserProfile;
import com.example.profile_service.repositories.UserProfileRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonGrpcServer extends PersonGrpcServiceGrpc.PersonGrpcServiceImplBase {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void searchPersons(SearchPersonsRequest request, StreamObserver<SearchPersonsResponse> responseObserver) {
        try {
            var profiles = userProfileRepository.findByPersonIdIn(request.getPersonIdsList());

            SearchPersonsResponse.Builder response = SearchPersonsResponse.newBuilder();
            for (UserProfile profile : profiles) {
                PersonSummary.Builder summary = PersonSummary.newBuilder()
                        .setPersonId(profile.getPersonId())
                        .setFullName(buildFullName(profile));
                if (profile.getGroup() != null) {
                    summary.setGroupTitle(profile.getGroup().getTitle());
                }
                response.addPersons(summary.build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    private String buildFullName(UserProfile profile) {
        StringBuilder name = new StringBuilder(profile.getLastName()).append(" ").append(profile.getFirstName());
        if (profile.getSecondName() != null && !profile.getSecondName().isBlank()) {
            name.append(" ").append(profile.getSecondName());
        }
        return name.toString();
    }
}
