package com.example.transactions_service.controllers;

import com.example.transactions_service.DTOs.*;
import com.example.transactions_service.exceptions.UnauthorizedException;
import com.example.transactions_service.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PROFORG_SCHOOL = "PROFORG_SCHOOL";

    private final TransactionService transactionService;

    /**
     * Вызывается Events Service при принятии заявки на мероприятие (Литвинов
     * фиксирует баллы за посещение).
     */
    @PostMapping("/event-reward")
    public ResponseEntity<TransactionResponseDTO> eventReward(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Person-Id", required = false) Long reviewerId,
            @Valid @RequestBody EventRewardRequestDTO dto) {
        requireAdmin(role, reviewerId);
        TransactionResponseDTO response = transactionService.eventReward(reviewerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Литвинов переводит баллы конкретному человеку или на рабочий кошелёк школы.
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Person-Id", required = false) Long callerId,
            @Valid @RequestBody TransferRequestDTO dto) {
        requireAdmin(role, callerId);
        TransactionResponseDTO response = transactionService.transferFromAdmin(callerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Профорг школы награждает студента за заслуги из рабочего кошелька своей школы.
     */
    @PostMapping("/school-award")
    public ResponseEntity<TransactionResponseDTO> schoolAward(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-School-Id", required = false) Long schoolId,
            @Valid @RequestBody SchoolAwardRequestDTO dto) {
        if (!ROLE_PROFORG_SCHOOL.equals(role) || schoolId == null) {
            throw new UnauthorizedException("Только профорг школы может награждать из кошелька школы");
        }
        TransactionResponseDTO response = transactionService.schoolAward(schoolId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Вызывается Shop Service при подтверждении покупки. Доверенный сервис-к-сервису
     * вызов (нет отдельной service-to-service аутентификации в MVP — известное
     * ограничение, см. итоговое сообщение).
     */
    @PostMapping("/purchase-charge")
    public ResponseEntity<TransactionResponseDTO> chargePurchase(@Valid @RequestBody PurchaseChargeRequestDTO dto) {
        TransactionResponseDTO response = transactionService.chargePurchase(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponseDTO> refund(@Valid @RequestBody PurchaseChargeRequestDTO dto) {
        TransactionResponseDTO response = transactionService.refundPurchase(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getHistory(@RequestParam UUID walletId) {
        return ResponseEntity.ok(transactionService.getHistoryByWalletId(walletId));
    }

    private void requireAdmin(String role, Long callerId) {
        if (!ROLE_ADMIN.equals(role) || callerId == null) {
            throw new UnauthorizedException("Доступно только администратору");
        }
    }
}
