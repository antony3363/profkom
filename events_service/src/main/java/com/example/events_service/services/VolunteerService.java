package com.example.events_service.services;


import com.example.events_service.DTOs.VolunteerRecordCreateRequestDTO;
import com.example.events_service.DTOs.VolunteerRecordResponseDTO;
import com.example.events_service.clients.PersonLookupDTO;
import com.example.events_service.clients.PersonServiceClient;
import com.example.events_service.entities.Event;
import com.example.events_service.entities.VolunteerRecord;
import com.example.events_service.exceptions.DuplicateRecordException;
import com.example.events_service.exceptions.EntityNotFoundException;
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

    private final VolunteerRepository volunteerRepository;
    private final EventRepository eventRepository;
    private final PersonServiceClient personServiceClient;

    @Transactional
    public VolunteerRecordResponseDTO createVolunteer(VolunteerRecordCreateRequestDTO dto) {

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

        volunteerRecord = volunteerRepository.save(volunteerRecord);
        return mapToResponseDTO(volunteerRecord);
    }


    @Transactional(readOnly = true)
    public VolunteerRecordResponseDTO getVolunteerById(UUID volunteerEntryId) {
        VolunteerRecord volunteerRecord = volunteerRepository.findById(volunteerEntryId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found with id: " + volunteerEntryId));
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
    public List<VolunteerRecordResponseDTO> getVolunteersByEventId(UUID eventId, String faculty) {
        List<VolunteerRecord> volunteerRecords = volunteerRepository.findAllByEvent_EventId(eventId);

        List<Long> personIds = volunteerRecords.stream()
                .map(VolunteerRecord::getLichnostId)
                .collect(Collectors.toList());

        List<PersonLookupDTO> persons = personServiceClient.searchPersons(personIds, faculty);

        Map<Long, VolunteerRecord> volunteersByPersonId = volunteerRecords.stream()
                .collect(Collectors.toMap(VolunteerRecord::getLichnostId, v -> v));

        // order comes from person-service (already sorted by name / filtered by faculty there)
        return persons.stream()
                .map(person -> mapToResponseDTO(volunteersByPersonId.get(person.personId()), person))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteVolunteer(UUID volunteerEntryId) {
        if (!volunteerRepository.existsById(volunteerEntryId)) {
            throw new EntityNotFoundException("Volunteer not found with id: " + volunteerEntryId);
        }
        volunteerRepository.deleteById(volunteerEntryId);
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
                .faculty(person.faculty())
                .build();
    }

}
