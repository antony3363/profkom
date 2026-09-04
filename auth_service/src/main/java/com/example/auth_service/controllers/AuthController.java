package com.example.auth_service.controllers;

import com.example.auth_service.DTOs.LoginRequestDTO;
import com.example.auth_service.DTOs.RefreshRequestDTO;
import com.example.auth_service.DTOs.TokenPairResponseDTO;
import com.example.auth_service.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * ЗАГЛУШКА реального обмена с SSO ТПУ — см. LoginRequestDTO.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto.getPersonId(), dto.getEmail(), dto.getFirstName(), dto.getLastName()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDTO dto) {
        authService.logout(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
