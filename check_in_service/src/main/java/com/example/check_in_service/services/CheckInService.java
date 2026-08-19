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
import com.example.check_in_service.qr.QrTokenService;
import com.example.check_in_service.qr.SegmentQrPayload;
import com.example.check_in_service.qr.SegmentQrService;
import com.example.check_in_service.repositories.CheckInRepository;
import com.example.check_in_service.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final RegistrationRepository registrationRepository;
    private final QrTokenService qrTokenService;
    private final SegmentQrService segmentQrService;

    @Transactional
    public CheckInResponseDTO createCheckIn(CheckInCreateRequestDTO dto, Long callerPersonId) {
        long personId;
        SegmentQrPayload segment;

        if (dto.getType() == CheckInType.SELF_SCAN) {
            if (callerPersonId == null) {
                throw new UnauthorizedException("Missing caller identity for self check-in");
            }
            personId = callerPersonId;
            segment = segmentQrService.parseSegmentPayload(dto.getQrPayload());
        } else {
            if (dto.getSegmentId() == null) {
                throw new InvalidQrPayloadException("segmentId is required for staff scan");
            }
            personId = qrTokenService.verifyAndExtractPersonId(dto.getQrPayload());
            segment = segmentQrService.resolveSegment(dto.getSegmentId());
        }

        Registration registration = registrationRepository
                .findByPersonIdAndEventId(personId, segment.eventId())
                .orElse(null);

        if (registration == null && segment.registrationRequired()) {
            throw new EntityNotFoundException(
                    "Person " + personId + " is not registered for event " + segment.eventId());
        }

        if (checkInRepository.existsByPersonIdAndSegmentId(personId, segment.segmentId())) {
            throw new DuplicateRecordException(
                    "Person " + personId + " is already checked in for segment " + segment.segmentId());
        }

        CheckIn checkIn = CheckIn.builder()
                .registration(registration)
                .personId(personId)
                .segmentId(segment.segmentId())
                .type(dto.getType())
                .build();

        checkIn = checkInRepository.save(checkIn);
        return mapToResponseDTO(checkIn);
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
    public List<CheckInResponseDTO> getCheckInsBySegmentId(UUID segmentId) {
        return checkInRepository.findBySegmentId(segmentId).stream()
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
                .segmentId(checkIn.getSegmentId())
                .type(checkIn.getType())
                .createdAt(checkIn.getCreatedAt())
                .build();
    }
}
