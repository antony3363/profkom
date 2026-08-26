package com.example.auth_service.controllers;

import com.example.auth_service.DTOs.VerifyResponseDTO;
import com.example.auth_service.exceptions.InvalidTokenException;
import com.example.auth_service.security.AccessTokenClaims;
import com.example.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Валидация access-токена. В целевой архитектуре это делает API Gateway сам по
 * публичному ключу — этого сервиса-шлюза пока нет в репозитории, поэтому downstream-
 * сервисы (или тестовый вызов) могут проверить токен здесь.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;

    @GetMapping("/verify")
    public ResponseEntity<VerifyResponseDTO> verify(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header must be a Bearer token");
        }
        String token = authorizationHeader.substring("Bearer ".length());
        AccessTokenClaims claims = jwtService.verify(token);
        return ResponseEntity.ok(VerifyResponseDTO.builder()
                .personId(claims.personId())
                .role(claims.role())
                .expiresAtEpochSeconds(claims.expiresAtEpochSeconds())
                .build());
    }
}
