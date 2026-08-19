package com.example.events_service.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Talks to the user/person microservice to resolve names & faculties for a bounded set of
 * personIds (e.g. the volunteers of one event), already filtered/sorted server-side there.
 * Contract proposed here ("POST /api/v1/persons/search") - needs to actually exist on the
 * person-service side, it's not part of this repo.
 */
@Component
@RequiredArgsConstructor
public class PersonServiceClient {

    private final RestClient personServiceRestClient;

    public List<PersonLookupDTO> searchPersons(List<Long> personIds, String faculty) {
        if (personIds.isEmpty()) {
            return List.of();
        }

        return personServiceRestClient.post()
                .uri("/api/v1/persons/search")
                .body(new PersonSearchRequest(personIds, faculty))
                .retrieve()
                .body(new ParameterizedTypeReference<List<PersonLookupDTO>>() {
                });
    }
}
