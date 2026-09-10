package com.example.auth_service.services;

import com.example.auth_service.DTOs.RoleUpdateRequestDTO;
import com.example.auth_service.DTOs.UserResponseDTO;
import com.example.auth_service.entities.AppUser;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exceptions.EntityNotFoundException;
import com.example.auth_service.kafka.UserRegisteredEvent;
import com.example.auth_service.kafka.UserRegisteredProducer;
import com.example.auth_service.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final UserRegisteredProducer userRegisteredProducer;

    /**
     * Первый вход — создаёт учётную запись со ролью STUDENT по умолчанию и
     * публикует UserRegistered в Kafka (слушает Profile Service — создаёт профиль,
     * если пришли email/имя; заглушка SSO ТПУ пока не обязана их присылать).
     */
    @Transactional
    public AppUser getOrCreateUser(long lichnostId, String email, String firstName, String lastName) {
        return appUserRepository.findByLichnostId(lichnostId)
                .orElseGet(() -> {
                    AppUser created = appUserRepository.saveAndFlush(AppUser.builder()
                            .lichnostId(lichnostId)
                            .role(UserRole.STUDENT)
                            .build());
                    userRegisteredProducer.publish(new UserRegisteredEvent(lichnostId, email, firstName, lastName));
                    return created;
                });
    }

    /**
     * Слушает school-proforg-changed из Profile Service: снимает роль
     * PROFORG_SCHOOL со старого профорга (если он ей действительно обладал по
     * этой же школе) и назначает её новому — учётная запись нового профорга
     * создаётся, если он ещё ни разу не логинился.
     */
    @Transactional
    public void applySchoolProforgChange(long schoolId, Long oldProforgId, Long newProforgId) {
        if (oldProforgId != null && !oldProforgId.equals(newProforgId)) {
            appUserRepository.findByLichnostId(oldProforgId).ifPresent(user -> {
                if (user.getRole() == UserRole.PROFORG_SCHOOL && Objects.equals(user.getSchoolId(), schoolId)) {
                    user.setRole(UserRole.STUDENT);
                    user.setSchoolId(null);
                    appUserRepository.saveAndFlush(user);
                }
            });
        }
        if (newProforgId != null) {
            AppUser user = appUserRepository.findByLichnostId(newProforgId)
                    .orElseGet(() -> AppUser.builder().lichnostId(newProforgId).role(UserRole.STUDENT).build());
            user.setRole(UserRole.PROFORG_SCHOOL);
            user.setSchoolId(schoolId);
            appUserRepository.saveAndFlush(user);
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        return mapToResponseDTO(findUser(userId));
    }

    @Transactional
    public UserResponseDTO updateRole(Long userId, RoleUpdateRequestDTO dto) {
        AppUser user = findUser(userId);
        user.setRole(dto.getRole());
        // schoolId имеет смысл только для PROFORG_SCHOOL — для остальных ролей чистим
        user.setSchoolId(dto.getRole() == UserRole.PROFORG_SCHOOL ? dto.getSchoolId() : null);
        user = appUserRepository.saveAndFlush(user);
        return mapToResponseDTO(user);
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private UserResponseDTO mapToResponseDTO(AppUser user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .lichnostId(user.getLichnostId())
                .role(user.getRole())
                .schoolId(user.getSchoolId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
