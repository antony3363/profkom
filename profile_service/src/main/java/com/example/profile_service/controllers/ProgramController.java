package com.example.profile_service.controllers;

import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.DTOs.ProgramCreateRequestDTO;
import com.example.profile_service.DTOs.ProgramResponseDTO;
import com.example.profile_service.exceptions.UnauthorizedException;
import com.example.profile_service.services.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class ProgramController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramResponseDTO> createProgram(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ProgramCreateRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.createProgram(dto));
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramResponseDTO> getProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getProgramById(programId));
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponseDTO>> getPrograms(@RequestParam Long schoolId) {
        return ResponseEntity.ok(programService.getProgramsBySchool(schoolId));
    }

    @PutMapping("/{programId}/proforg")
    public ResponseEntity<ProgramResponseDTO> assignProforg(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long programId,
            @Valid @RequestBody ProforgAssignRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(programService.assignProforg(programId, dto));
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет справочником направлений");
        }
    }
}
