package com.example.profile_service.controllers;

import com.example.profile_service.DTOs.UserProfileCreateRequestDTO;
import com.example.profile_service.DTOs.UserProfileResponseDTO;
import com.example.profile_service.DTOs.UserProfileUpdateRequestDTO;
import com.example.profile_service.exceptions.UnauthorizedException;
import com.example.profile_service.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * В целевой архитектуре создаётся автоматически по событию UserRegistered из
     * Auth Service — открытый POST оставлен как временная замена, пока Kafka не
     * подключена.
     */
    @PostMapping
    public ResponseEntity<UserProfileResponseDTO> createProfile(@Valid @RequestBody UserProfileCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.createProfile(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long lichnostId) {
        if (lichnostId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        return ResponseEntity.ok(userProfileService.getProfileById(lichnostId));
    }

    @GetMapping("/{lichnostId}")
    public ResponseEntity<UserProfileResponseDTO> getProfile(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long callerId,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @PathVariable Long lichnostId) {
        return ResponseEntity.ok(userProfileService.getProfileById(lichnostId, role, callerId, callerSchoolId));
    }

    @GetMapping
    public ResponseEntity<List<UserProfileResponseDTO>> getProfilesByGroup(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long callerSchoolId,
            @RequestParam Long groupId) {
        return ResponseEntity.ok(userProfileService.getProfilesByGroup(groupId, role, callerSchoolId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long lichnostId,
            @Valid @RequestBody UserProfileUpdateRequestDTO dto) {
        if (lichnostId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        // студент не может сам сменить себе membershipStatus — это право администратора
        dto.setMembershipStatus(null);
        return ResponseEntity.ok(userProfileService.updateProfile(lichnostId, dto));
    }

    @PutMapping("/{lichnostId}")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long lichnostId,
            @Valid @RequestBody UserProfileUpdateRequestDTO dto) {
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Изменение статуса членства доступно только администратору");
        }
        return ResponseEntity.ok(userProfileService.updateProfile(lichnostId, dto));
    }
}
