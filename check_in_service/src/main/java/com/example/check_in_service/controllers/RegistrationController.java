package com.example.check_in_service.controllers;

import com.example.check_in_service.DTOs.RegistrationCreateRequestDTO;
import com.example.check_in_service.DTOs.RegistrationResponseDTO;
import com.example.check_in_service.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> createRegistration(@Valid @RequestBody RegistrationCreateRequestDTO dto) {
        RegistrationResponseDTO response = registrationService.createRegistration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<RegistrationResponseDTO> getRegistration(@PathVariable UUID registrationId) {
        RegistrationResponseDTO response = registrationService.getRegistrationById(registrationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RegistrationResponseDTO>> getRegistrations(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) Long personId) {
        List<RegistrationResponseDTO> registrations;
        if (eventId != null) {
            registrations = registrationService.getRegistrationsByEventId(eventId);
        } else if (personId != null) {
            registrations = registrationService.getRegistrationsByPersonId(personId);
        } else {
            registrations = registrationService.getAllRegistrations();
        }
        return ResponseEntity.ok(registrations);
    }

    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable UUID registrationId) {
        registrationService.deleteRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }
}
