package com.example.events_service.exceptions;

import lombok.Getter;

@Getter
public class InvalidEventDataException extends RuntimeException {

    private final String field;

    public InvalidEventDataException(String field, String message) {
        super(message);
        this.field = field;
    }
}
