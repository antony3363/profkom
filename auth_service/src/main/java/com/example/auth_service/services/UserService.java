package com.example.auth_service.services;

import com.example.auth_service.DTOs.RoleUpdateRequestDTO;
import com.example.auth_service.DTOs.UserResponseDTO;
import com.example.auth_service.entities.AppUser;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exceptions.EntityNotFoundException;
import com.example.auth_service.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;

    /**
     * Первый вход — создаёт учётную запись со ролью STUDENT по умолчанию (в целевой
     * архитектуре триггерит UserRegistered в Kafka, которое слушает Profile Service —
     * пока не подключено).
     */
    @Transactional
    public AppUser getOrCreateUser(long personId) {
        return appUserRepository.findByPersonId(personId)
                .orElseGet(() -> appUserRepository.save(AppUser.builder()
                        .personId(personId)
                        .role(UserRole.STUDENT)
                        .build()));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        return mapToResponseDTO(findUser(userId));
    }

    @Transactional
    public UserResponseDTO updateRole(UUID userId, RoleUpdateRequestDTO dto) {
        AppUser user = findUser(userId);
        user.setRole(dto.getRole());
        user = appUserRepository.save(user);
        return mapToResponseDTO(user);
    }

    private AppUser findUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private UserResponseDTO mapToResponseDTO(AppUser user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .personId(user.getPersonId())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
