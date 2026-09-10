package com.example.auth_service.controllers;

import com.example.auth_service.DTOs.VerifyResponseDTO;
import com.example.auth_service.exceptions.InvalidTokenException;
import com.example.auth_service.security.AccessTokenClaims;
import com.example.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Валидация access-токена по схеме интроспекции — gateway_service вызывает этот
 * эндпоинт на каждый запрос вместо локальной проверки подписи по публичному ключу
 * (проще: не нужно распространять/публиковать RSA-ключ отдельным JWKS-эндпоинтом).
 * Компромисс — лишний сетевой вызов на каждый запрос через Gateway; при таком
 * масштабе (внутренний сервис профкома) это приемлемо.
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
                .lichnostId(claims.lichnostId())
                .role(claims.role())
                .schoolId(claims.schoolId())
                .expiresAtEpochSeconds(claims.expiresAtEpochSeconds())
                .build());
    }
}
