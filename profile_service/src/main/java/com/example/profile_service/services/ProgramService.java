package com.example.profile_service.services;

import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.DTOs.ProgramCreateRequestDTO;
import com.example.profile_service.DTOs.ProgramResponseDTO;
import com.example.profile_service.entities.Program;
import com.example.profile_service.entities.School;
import com.example.profile_service.exceptions.EntityNotFoundException;
import com.example.profile_service.repositories.ProgramRepository;
import com.example.profile_service.repositories.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public ProgramResponseDTO createProgram(ProgramCreateRequestDTO dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new EntityNotFoundException("School not found with id: " + dto.getSchoolId()));

        Program program = Program.builder().title(dto.getTitle()).school(school).build();
        program = programRepository.saveAndFlush(program);
        return mapToResponseDTO(program);
    }

    @Transactional(readOnly = true)
    public ProgramResponseDTO getProgramById(Long programId) {
        return mapToResponseDTO(findProgram(programId));
    }

    @Transactional(readOnly = true)
    public List<ProgramResponseDTO> getProgramsBySchool(Long schoolId) {
        return programRepository.findBySchool_SchoolId(schoolId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Почётное звание — не даёт функционала, в отличие от School.proforgId.
     */
    @Transactional
    public ProgramResponseDTO assignProforg(Long programId, ProforgAssignRequestDTO dto) {
        Program program = findProgram(programId);
        program.setProforgId(dto.getProforgId());
        program = programRepository.saveAndFlush(program);
        return mapToResponseDTO(program);
    }

    private Program findProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found with id: " + programId));
    }

    private ProgramResponseDTO mapToResponseDTO(Program program) {
        return ProgramResponseDTO.builder()
                .programId(program.getProgramId())
                .title(program.getTitle())
                .schoolId(program.getSchool().getSchoolId())
                .proforgId(program.getProforgId())
                .createdAt(program.getCreatedAt())
                .build();
    }
}
