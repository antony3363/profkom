package com.example.profile_service.services;

import com.example.profile_service.DTOs.GroupCreateRequestDTO;
import com.example.profile_service.DTOs.GroupResponseDTO;
import com.example.profile_service.DTOs.ProforgAssignRequestDTO;
import com.example.profile_service.entities.Group;
import com.example.profile_service.entities.Program;
import com.example.profile_service.exceptions.EntityNotFoundException;
import com.example.profile_service.repositories.GroupRepository;
import com.example.profile_service.repositories.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final ProgramRepository programRepository;

    @Transactional
    public GroupResponseDTO createGroup(GroupCreateRequestDTO dto) {
        Program program = programRepository.findById(dto.getProgramId())
                .orElseThrow(() -> new EntityNotFoundException("Program not found with id: " + dto.getProgramId()));

        Group group = Group.builder().title(dto.getTitle()).program(program).build();
        group = groupRepository.save(group);
        return mapToResponseDTO(group);
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(Long groupId) {
        return mapToResponseDTO(findGroup(groupId));
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByProgram(Long programId) {
        return groupRepository.findByProgram_ProgramId(programId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Почётное звание — не даёт функционала, в отличие от School.proforgId.
     */
    @Transactional
    public GroupResponseDTO assignProforg(Long groupId, ProforgAssignRequestDTO dto) {
        Group group = findGroup(groupId);
        group.setProforgId(dto.getProforgId());
        group = groupRepository.save(group);
        return mapToResponseDTO(group);
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
    }

    private GroupResponseDTO mapToResponseDTO(Group group) {
        return GroupResponseDTO.builder()
                .groupId(group.getGroupId())
                .title(group.getTitle())
                .programId(group.getProgram().getProgramId())
                .proforgId(group.getProforgId())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
