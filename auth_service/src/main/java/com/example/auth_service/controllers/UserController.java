package com.example.auth_service.controllers;

import com.example.auth_service.DTOs.RoleUpdateRequestDTO;
import com.example.auth_service.DTOs.UserResponseDTO;
import com.example.auth_service.exceptions.UnauthorizedException;
import com.example.auth_service.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Временная замена автосинхронизации роли PROFORG_SCHOOL через Kafka
     * (см. RoleUpdateRequestDTO).
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<UserResponseDTO> updateRole(
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @PathVariable Long userId,
            @Valid @RequestBody RoleUpdateRequestDTO dto) {
        if (!ROLE_ADMIN.equals(callerRole)) {
            throw new UnauthorizedException("Только администратор может менять роли");
        }
        return ResponseEntity.ok(userService.updateRole(userId, dto));
    }
}
