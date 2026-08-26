package com.example.check_in_service.services;

import com.example.check_in_service.DTOs.CheckInCreateRequestDTO;
import com.example.check_in_service.DTOs.CheckInResponseDTO;
import com.example.check_in_service.entities.CheckIn;
import com.example.check_in_service.entities.Registration;
import com.example.check_in_service.enums.CheckInType;
import com.example.check_in_service.exceptions.DuplicateRecordException;
import com.example.check_in_service.exceptions.EntityNotFoundException;
import com.example.check_in_service.exceptions.InvalidQrPayloadException;
import com.example.check_in_service.exceptions.UnauthorizedException;
import com.example.check_in_service.grpc.EventDetails;
import com.example.check_in_service.grpc.EventGrpcClient;
import com.example.check_in_service.grpc.TransactionGrpcClient;
import com.example.check_in_service.qr.EventQrPayload;
import com.example.check_in_service.qr.EventQrService;
import com.example.check_in_service.qr.QrTokenService;
import com.example.check_in_service.repositories.CheckInRepository;
import com.example.check_in_service.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final RegistrationRepository registrationRepository;
    private final QrTokenService qrTokenService;
    private final EventQrService eventQrService;
    private final EventGrpcClient eventGrpcClient;
    private final TransactionGrpcClient transactionGrpcClient;

    @Transactional
    public CheckInResponseDTO createCheckIn(CheckInCreateRequestDTO dto, Long callerPersonId) {
        long personId;
        UUID eventId;

        if (dto.getType() == CheckInType.SELF_SCAN) {
            if (callerPersonId == null) {
                throw new UnauthorizedException("Missing caller identity for self check-in");
            }
            personId = callerPersonId;
            EventQrPayload parsed = eventQrService.parseEventPayload(dto.getQrPayload());
            eventId = parsed.eventId();
        } else {
            if (dto.getEventId() == null) {
                throw new InvalidQrPayloadException("eventId is required for staff scan");
            }
            personId = qrTokenService.verifyAndExtractPersonId(dto.getQrPayload());
            eventId = dto.getEventId();
        }

        // всегда берём актуальные данные о мероприятии из events_service, а не только
        // то, что было закодировано в QR на момент его генерации
        EventDetails event = eventGrpcClient.getEventDetails(eventId);

        Registration registration = registrationRepository
                .findByPersonIdAndEventId(personId, eventId)
                .orElse(null);

        if (registration == null && event.registrationRequired()) {
            throw new EntityNotFoundException(
                    "Person " + personId + " is not registered for event " + eventId);
        }

        if (checkInRepository.existsByPersonIdAndEventId(personId, eventId)) {
            throw new DuplicateRecordException(
                    "Person " + personId + " is already checked in for event " + eventId);
        }

        CheckIn checkIn = CheckIn.builder()
                .registration(registration)
                .personId(personId)
                .eventId(eventId)
                .type(dto.getType())
                .build();

        checkIn = checkInRepository.save(checkIn);

        awardPointsIfApproved(event, personId);

        return mapToResponseDTO(checkIn);
    }

    /**
     * Начисление баллов — сайд-эффект, не должен ронять сам чек-ин, если
     * Transactions Service недоступен (посещение уже зафиксировано). Известное
     * ограничение MVP: без outbox/повторных попыток — если вызов не удался,
     * баллы придётся начислить вручную.
     */
    private void awardPointsIfApproved(EventDetails event, long personId) {
        if (event.pointsPerAttendee() == null || event.pointsPerAttendee() <= 0 || event.reviewedBy() == null) {
            return;
        }
        try {
            transactionGrpcClient.awardEventReward(event.reviewedBy(), personId, event.pointsPerAttendee(), event.eventId());
        } catch (Exception e) {
            log.error("Failed to award event reward for person {} / event {}", personId, event.eventId(), e);
        }
    }

    @Transactional(readOnly = true)
    public CheckInResponseDTO getCheckInById(UUID checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new EntityNotFoundException("CheckIn not found with id: " + checkInId));
        return mapToResponseDTO(checkIn);
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> getAllCheckIns() {
        return checkInRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> getCheckInsByRegistrationId(UUID registrationId) {
        return checkInRepository.findByRegistration_RegistrationId(registrationId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> getCheckInsByEventId(UUID eventId) {
        return checkInRepository.findByEventId(eventId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCheckIn(UUID checkInId) {
        if (!checkInRepository.existsById(checkInId)) {
            throw new EntityNotFoundException("CheckIn not found with id: " + checkInId);
        }
        checkInRepository.deleteById(checkInId);
    }

    private CheckInResponseDTO mapToResponseDTO(CheckIn checkIn) {
        return CheckInResponseDTO.builder()
                .checkInId(checkIn.getCheckInId())
                .registrationId(checkIn.getRegistration() != null ? checkIn.getRegistration().getRegistrationId() : null)
                .personId(checkIn.getPersonId())
                .eventId(checkIn.getEventId())
                .type(checkIn.getType())
                .createdAt(checkIn.getCreatedAt())
                .build();
    }
}
