package com.example.check_in_service.services;

import com.example.check_in_service.DTOs.RegistrationCreateRequestDTO;
import com.example.check_in_service.DTOs.RegistrationResponseDTO;
import com.example.check_in_service.entities.Registration;
import com.example.check_in_service.exceptions.DuplicateRecordException;
import com.example.check_in_service.exceptions.EntityNotFoundException;
import com.example.check_in_service.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    @Transactional
    public RegistrationResponseDTO createRegistration(RegistrationCreateRequestDTO dto) {
        if (registrationRepository.existsByPersonIdAndEventId(dto.getPersonId(), dto.getEventId())) {
            throw new DuplicateRecordException(
                    "Person " + dto.getPersonId() + " is already registered for event " + dto.getEventId());
        }

        Registration registration = Registration.builder()
                .personId(dto.getPersonId())
                .eventId(dto.getEventId())
                .build();

        registration = registrationRepository.save(registration);
        return mapToResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationById(UUID registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with id: " + registrationId));
        return mapToResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByEventId(UUID eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByPersonId(long personId) {
        return registrationRepository.findByPersonId(personId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRegistration(UUID registrationId) {
        if (!registrationRepository.existsById(registrationId)) {
            throw new EntityNotFoundException("Registration not found with id: " + registrationId);
        }
        registrationRepository.deleteById(registrationId);
    }

    private RegistrationResponseDTO mapToResponseDTO(Registration registration) {
        return RegistrationResponseDTO.builder()
                .registrationId(registration.getRegistrationId())
                .personId(registration.getPersonId())
                .eventId(registration.getEventId())
                .createdAt(registration.getCreatedAt())
                .build();
    }
}
