package com.example.profile_service.services;

import com.example.profile_service.DTOs.UserProfileCreateRequestDTO;
import com.example.profile_service.DTOs.UserProfileResponseDTO;
import com.example.profile_service.DTOs.UserProfileUpdateRequestDTO;
import com.example.profile_service.entities.Group;
import com.example.profile_service.entities.UserProfile;
import com.example.profile_service.exceptions.DuplicateRecordException;
import com.example.profile_service.exceptions.EntityNotFoundException;
import com.example.profile_service.repositories.GroupRepository;
import com.example.profile_service.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final GroupRepository groupRepository;

    /**
     * В целевой архитектуре создаётся Profile Service автоматически по событию
     * UserRegistered из Auth Service — пока Kafka не подключена, personId и остальные
     * поля передаются явно (например, из атрибутов SSO ТПУ на момент первого входа).
     */
    @Transactional
    public UserProfileResponseDTO createProfile(UserProfileCreateRequestDTO dto) {
        if (userProfileRepository.existsById(dto.getPersonId())) {
            throw new DuplicateRecordException("Profile already exists for person: " + dto.getPersonId());
        }
        if (userProfileRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateRecordException("Profile with email " + dto.getEmail() + " already exists");
        }
        if (dto.getCardNumber() != null && userProfileRepository.existsByCardNumber(dto.getCardNumber())) {
            throw new DuplicateRecordException("Profile with card number " + dto.getCardNumber() + " already exists");
        }

        Group group = resolveGroup(dto.getGroupId());

        UserProfile profile = UserProfile.builder()
                .personId(dto.getPersonId())
                .group(group)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .secondName(dto.getSecondName())
                .email(dto.getEmail())
                .cardNumber(dto.getCardNumber())
                .image(dto.getImage())
                .build();

        profile = userProfileRepository.saveAndFlush(profile);
        return mapToResponseDTO(profile);
    }

    /**
     * Вызывается consumer'ом user-registered из Kafka. В отличие от createProfile
     * (админский/тестовый путь) — молча пропускает, если профиль уже есть (не
     * должно происходить в норме, но событие может доставиться повторно) вместо
     * ошибки, так как здесь нет вызывающей стороны, которой можно вернуть 409.
     */
    @Transactional
    public void createFromRegistration(long personId, String email, String firstName, String lastName) {
        if (userProfileRepository.existsById(personId) || userProfileRepository.existsByEmail(email)) {
            return;
        }
        UserProfile profile = UserProfile.builder()
                .personId(personId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
        userProfileRepository.saveAndFlush(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfileById(Long personId) {
        return mapToResponseDTO(findProfile(personId));
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDTO> getProfilesByGroup(Long groupId) {
        return userProfileRepository.findByGroup_GroupId(groupId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(Long personId, UserProfileUpdateRequestDTO dto) {
        UserProfile profile = findProfile(personId);

        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getSecondName() != null) profile.setSecondName(dto.getSecondName());
        if (dto.getImage() != null) profile.setImage(dto.getImage());
        if (dto.getMembershipStatus() != null) profile.setMembershipStatus(dto.getMembershipStatus());

        if (dto.getEmail() != null && !dto.getEmail().equals(profile.getEmail())) {
            if (userProfileRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateRecordException("Profile with email " + dto.getEmail() + " already exists");
            }
            profile.setEmail(dto.getEmail());
        }

        if (dto.getCardNumber() != null && !dto.getCardNumber().equals(profile.getCardNumber())) {
            if (userProfileRepository.existsByCardNumber(dto.getCardNumber())) {
                throw new DuplicateRecordException("Profile with card number " + dto.getCardNumber() + " already exists");
            }
            profile.setCardNumber(dto.getCardNumber());
        }

        if (dto.getGroupId() != null) {
            profile.setGroup(resolveGroup(dto.getGroupId()));
        }

        profile = userProfileRepository.saveAndFlush(profile);
        return mapToResponseDTO(profile);
    }

    private Group resolveGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
    }

    private UserProfile findProfile(Long personId) {
        return userProfileRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for person: " + personId));
    }

    private UserProfileResponseDTO mapToResponseDTO(UserProfile profile) {
        return UserProfileResponseDTO.builder()
                .personId(profile.getPersonId())
                .groupId(profile.getGroup() != null ? profile.getGroup().getGroupId() : null)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .secondName(profile.getSecondName())
                .email(profile.getEmail())
                .cardNumber(profile.getCardNumber())
                .image(profile.getImage())
                .membershipStatus(profile.getMembershipStatus())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
