package com.example.profile_service.services;

import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.DTOs.SchoolCreateRequestDTO;
import com.example.profile_service.DTOs.SchoolResponseDTO;
import com.example.profile_service.entities.School;
import com.example.profile_service.exceptions.EntityNotFoundException;
import com.example.profile_service.repositories.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    @Transactional
    public SchoolResponseDTO createSchool(SchoolCreateRequestDTO dto) {
        School school = School.builder().title(dto.getTitle()).build();
        school = schoolRepository.save(school);
        return mapToResponseDTO(school);
    }

    @Transactional(readOnly = true)
    public SchoolResponseDTO getSchoolById(Long schoolId) {
        return mapToResponseDTO(findSchool(schoolId));
    }

    @Transactional(readOnly = true)
    public List<SchoolResponseDTO> getAllSchools() {
        return schoolRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Смена профорга школы — права (переводы со счёта школы, заявки на мероприятия)
     * передаются вместе с должностью, ничего не нужно переносить отдельно.
     */
    @Transactional
    public SchoolResponseDTO assignProforg(Long schoolId, ProforgAssignRequestDTO dto) {
        School school = findSchool(schoolId);
        school.setProforgId(dto.getProforgId());
        school = schoolRepository.save(school);
        return mapToResponseDTO(school);
    }

    private School findSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("School not found with id: " + schoolId));
    }

    private SchoolResponseDTO mapToResponseDTO(School school) {
        return SchoolResponseDTO.builder()
                .schoolId(school.getSchoolId())
                .title(school.getTitle())
                .proforgId(school.getProforgId())
                .createdAt(school.getCreatedAt())
                .build();
    }
}
