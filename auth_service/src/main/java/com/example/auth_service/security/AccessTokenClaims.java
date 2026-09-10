package com.example.auth_service.security;

import com.example.auth_service.enums.UserRole;

public record AccessTokenClaims(long lichnostId, UserRole role, Long schoolId, long expiresAtEpochSeconds) {
}
