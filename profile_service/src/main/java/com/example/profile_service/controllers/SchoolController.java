package com.example.profile_service.controllers;

import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.DTOs.SchoolCreateRequestDTO;
import com.example.profile_service.DTOs.SchoolResponseDTO;
import com.example.profile_service.exceptions.UnauthorizedException;
import com.example.profile_service.services.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final SchoolService schoolService;

    @PostMapping
    public ResponseEntity<SchoolResponseDTO> createSchool(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody SchoolCreateRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchool(dto));
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<SchoolResponseDTO> getSchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(schoolService.getSchoolById(schoolId));
    }

    @GetMapping
    public ResponseEntity<List<SchoolResponseDTO>> getSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @PutMapping("/{schoolId}/proforg")
    public ResponseEntity<SchoolResponseDTO> assignProforg(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long schoolId,
            @Valid @RequestBody ProforgAssignRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(schoolService.assignProforg(schoolId, dto));
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет справочником школ");
        }
    }
}
