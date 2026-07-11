package com.example.events_service.service;


import com.example.events_service.dto.VolunteerRecordCreateRequestDTO;
import com.example.events_service.dto.VolunteerRecordResponseDTO;
import com.example.events_service.dto.VolunteerRecordUpdateRequestDTO;
import com.example.events_service.entity.Event;
import com.example.events_service.entity.VolunteerRecord;
import com.example.events_service.repository.EventRepository;
import com.example.events_service.repository.VolunteerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final EventRepository eventRepository;

    @Transactional
    public VolunteerRecordResponseDTO createVolunteer(VolunteerRecordCreateRequestDTO dto) {

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + dto.getEventId()));

        VolunteerRecord volunteerRecord = VolunteerRecord.builder()
                .personId(dto.getPersonId())
                .event(event)
                .role(dto.getRole())
                .build();

        volunteerRecord = volunteerRepository.save(volunteerRecord);
        return mapToResponseDTO(volunteerRecord);
    }


    @Transactional(readOnly = true)
    public VolunteerRecordResponseDTO getVolunteerById(UUID volunteerEntryId) {
        VolunteerRecord volunteerRecord = volunteerRepository.findById(volunteerEntryId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerEntryId));
        return mapToResponseDTO(volunteerRecord);
    }

    @Transactional(readOnly = true)
    public List<VolunteerRecordResponseDTO> getAllVolunteers() {
        List<VolunteerRecord> volunteerRecords = volunteerRepository.findAll();
        return volunteerRecords.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<VolunteerRecordResponseDTO> getVolunteersByEventId(UUID eventId) {
        List<VolunteerRecord> volunteerRecords = volunteerRepository.findAllByEvent_EventId(eventId);
        return volunteerRecords.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public VolunteerRecordResponseDTO updateVolunteer(UUID volunteerEntryId, VolunteerRecordUpdateRequestDTO dto) {
        VolunteerRecord volunteerRecord = volunteerRepository.findById(volunteerEntryId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerEntryId));

        if (dto.getRole() != null) volunteerRecord.setRole(dto.getRole());

        volunteerRecord = volunteerRepository.save(volunteerRecord);
        return mapToResponseDTO(volunteerRecord);
    }

    @Transactional
    public void deleteVolunteer(UUID volunteerEntryId) {
        if (!volunteerRepository.existsById(volunteerEntryId)) {
            throw new RuntimeException("Volunteer not found with id: " + volunteerEntryId);
        }
        volunteerRepository.deleteById(volunteerEntryId);
    }




    private VolunteerRecordResponseDTO mapToResponseDTO(VolunteerRecord volunteerRecord) {
        return VolunteerRecordResponseDTO.builder()
                .volunteerEntryId(volunteerRecord.getVolunteerRecordId())
                .personId(volunteerRecord.getPersonId())
                .eventId(volunteerRecord.getEvent().getEventId())
                .role(volunteerRecord.getRole())
                .build();
    }

}
