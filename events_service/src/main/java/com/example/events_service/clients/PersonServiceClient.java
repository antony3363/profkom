package com.example.events_service.clients;

import com.example.profile_service.grpc.PersonGrpcServiceGrpc;
import com.example.profile_service.grpc.PersonSummary;
import com.example.profile_service.grpc.SearchPersonsRequest;
import com.example.profile_service.grpc.SearchPersonsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC-клиент к profile_service. Раньше был REST-вызовом к вымышленному
 * "person-service" — теперь целится в реально существующий profile_service.
 */
@Component
@RequiredArgsConstructor
public class PersonServiceClient {

    private final PersonGrpcServiceGrpc.PersonGrpcServiceBlockingStub personGrpcServiceBlockingStub;

    public List<PersonLookupDTO> searchPersons(List<Long> lichnostIds) {
        if (lichnostIds.isEmpty()) {
            return List.of();
        }

        SearchPersonsRequest request = SearchPersonsRequest.newBuilder()
                .addAllLichnostIds(lichnostIds)
                .build();

        SearchPersonsResponse response = personGrpcServiceBlockingStub.searchPersons(request);

        return response.getPersonsList().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PersonLookupDTO toDto(PersonSummary summary) {
        return new PersonLookupDTO(
                summary.getLichnostId(),
                summary.getFullName(),
                summary.hasGroupTitle() ? summary.getGroupTitle() : null);
    }
}
