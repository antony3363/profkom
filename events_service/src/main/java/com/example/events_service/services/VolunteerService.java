package com.example.events_service.services;


import com.example.events_service.DTOs.VolunteerRecordCreateRequestDTO;
import com.example.events_service.DTOs.VolunteerRecordResponseDTO;
import com.example.events_service.clients.PersonLookupDTO;
import com.example.events_service.clients.PersonServiceClient;
import com.example.events_service.entities.Event;
import com.example.events_service.entities.VolunteerRecord;
import com.example.events_service.exceptions.DuplicateRecordException;
import com.example.events_service.exceptions.EntityNotFoundException;
import com.example.events_service.exceptions.UnauthorizedException;
import com.example.events_service.repositories.EventRepository;
import com.example.events_service.repositories.VolunteerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PROFORG_SCHOOL = "PROFORG_SCHOOL";

    private final VolunteerRepository volunteerRepository;
    private final EventRepository eventRepository;
    private final PersonServiceClient personServiceClient;

    @Transactional
    public VolunteerRecordResponseDTO createVolunteer(VolunteerRecordCreateRequestDTO dto, String role, Long callerId) {
        if (!ROLE_ADMIN.equals(role) && (callerId == null || !callerId.equals(dto.getLichnostId()))) {
            throw new UnauthorizedException("Записаться волонтёром можно только за себя");
        }

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + dto.getEventId()));

        if (volunteerRepository.existsByEvent_EventIdAndLichnostId(dto.getEventId(), dto.getLichnostId())) {
            throw new DuplicateRecordException(
                    "Volunteer " + dto.getLichnostId() + " is already registered for event " + dto.getEventId());
        }

        VolunteerRecord volunteerRecord = VolunteerRecord.builder()
                .lichnostId(dto.getLichnostId())
                .event(event)
                .build();

        volunteerRecord = volunteerRepository.saveAndFlush(volunteerRecord);
        return mapToResponseDTO(volunteerRecord);
    }


    @Transactional(readOnly = true)
    public VolunteerRecordResponseDTO getVolunteerById(UUID volunteerEntryId, String role, Long callerId, Long callerSchoolId) {
        VolunteerRecord volunteerRecord = volunteerRepository.findById(volunteerEntryId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found with id: " + volunteerEntryId));
        requireOwnerOrSchoolRepOrAdmin(volunteerRecord, role, callerId, callerSchoolId);
        return mapToResponseDTO(volunteerRecord);
    }

    @Transactional(readOnly = true)
    public List<VolunteerRecordResponseDTO> getAllVolunteers(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Полный список волонтёров доступен только администратору");
        }
        List<VolunteerRecord> volunteerRecords = volunteerRepository.findAll();
        return volunteerRecords.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<VolunteerRecordResponseDTO> getVolunteersByEventId(UUID eventId, String role, Long callerSchoolId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        if (!ROLE_ADMIN.equals(role)
                && !(ROLE_PROFORG_SCHOOL.equals(role) && callerSchoolId != null && callerSchoolId.equals(event.getSchoolId()))) {
            throw new UnauthorizedException("Список волонтёров мероприятия доступен только профоргу его школы или администратору");
        }

        List<VolunteerRecord> volunteerRecords = volunteerRepository.findAllByEvent_EventId(eventId);

        List<Long> lichnostIds = volunteerRecords.stream()
                .map(VolunteerRecord::getLichnostId)
                .collect(Collectors.toList());

        List<PersonLookupDTO> persons = personServiceClient.searchPersons(lichnostIds);

        Map<Long, VolunteerRecord> volunteersByLichnostId = volunteerRecords.stream()
                .collect(Collectors.toMap(VolunteerRecord::getLichnostId, v -> v));

        // order comes from person-service (already sorted by name / filtered by faculty there)
        return persons.stream()
                .map(person -> mapToResponseDTO(volunteersByLichnostId.get(person.lichnostId()), person))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteVolunteer(UUID volunteerEntryId, String role, Long callerId) {
        VolunteerRecord volunteerRecord = volunteerRepository.findById(volunteerEntryId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found with id: " + volunteerEntryId));
        if (!ROLE_ADMIN.equals(role) && (callerId == null || callerId != volunteerRecord.getLichnostId())) {
            throw new UnauthorizedException("Отменить запись волонтёра может только он сам или администратор");
        }
        volunteerRepository.deleteById(volunteerEntryId);
    }

    private void requireOwnerOrSchoolRepOrAdmin(VolunteerRecord volunteerRecord, String role, Long callerId, Long callerSchoolId) {
        if (ROLE_ADMIN.equals(role)) {
            return;
        }
        if (callerId != null && callerId == volunteerRecord.getLichnostId()) {
            return;
        }
        if (ROLE_PROFORG_SCHOOL.equals(role) && callerSchoolId != null
                && callerSchoolId.equals(volunteerRecord.getEvent().getSchoolId())) {
            return;
        }
        throw new UnauthorizedException("Недостаточно прав для просмотра этой записи волонтёра");
    }




    private VolunteerRecordResponseDTO mapToResponseDTO(VolunteerRecord volunteerRecord) {
        return VolunteerRecordResponseDTO.builder()
                .volunteerEntryId(volunteerRecord.getVolunteerRecordId())
                .lichnostId(volunteerRecord.getLichnostId())
                .eventId(volunteerRecord.getEvent().getEventId())
                .build();
    }

    private VolunteerRecordResponseDTO mapToResponseDTO(VolunteerRecord volunteerRecord, PersonLookupDTO person) {
        return VolunteerRecordResponseDTO.builder()
                .volunteerEntryId(volunteerRecord.getVolunteerRecordId())
                .lichnostId(volunteerRecord.getLichnostId())
                .eventId(volunteerRecord.getEvent().getEventId())
                .fullName(person.fullName())
                .groupTitle(person.groupTitle())
                .build();
    }

}
