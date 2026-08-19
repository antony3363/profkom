package com.example.events_service.clients;

import java.util.List;

public record PersonSearchRequest(List<Long> personIds, String faculty) {
}
