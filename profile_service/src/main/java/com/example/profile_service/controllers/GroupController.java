package com.example.profile_service.controllers;

import com.example.profile_service.DTOs.GroupCreateRequestDTO;
import com.example.profile_service.DTOs.GroupResponseDTO;
import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.exceptions.UnauthorizedException;
import com.example.profile_service.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody GroupCreateRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(dto));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDTO> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getGroups(@RequestParam Long programId) {
        return ResponseEntity.ok(groupService.getGroupsByProgram(programId));
    }

    @PutMapping("/{groupId}/proforg")
    public ResponseEntity<GroupResponseDTO> assignProforg(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long groupId,
            @Valid @RequestBody ProforgAssignRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(groupService.assignProforg(groupId, dto));
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет справочником групп");
        }
    }
}
