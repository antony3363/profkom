package com.example.check_in_service.exceptions;

public class InvalidQrPayloadException extends RuntimeException {
    public InvalidQrPayloadException(String message) {
        super(message);
    }
}
