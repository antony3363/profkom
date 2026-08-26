package com.example.shop_service.controllers;

import com.example.shop_service.DTOs.PurchaseCreateRequestDTO;
import com.example.shop_service.DTOs.PurchaseResponseDTO;
import com.example.shop_service.services.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponseDTO> createPurchase(
            @RequestHeader(value = "X-Person-Id", required = false) Long buyerId,
            @Valid @RequestBody PurchaseCreateRequestDTO dto) {
        if (buyerId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Person-Id header");
        }
        PurchaseResponseDTO response = purchaseService.createPurchase(buyerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponseDTO> getPurchase(@PathVariable UUID purchaseId) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(purchaseId));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseResponseDTO>> getMyPurchases(
            @RequestHeader(value = "X-Person-Id", required = false) Long buyerId) {
        if (buyerId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Person-Id header");
        }
        return ResponseEntity.ok(purchaseService.getPurchasesByBuyerId(buyerId));
    }

    /**
     * Вызывается Transactions Service (или оркестрирующим слоем) после успешного
     * списания баллов.
     */
    @PostMapping("/{purchaseId}/confirm")
    public ResponseEntity<PurchaseResponseDTO> confirmPurchase(
            @PathVariable UUID purchaseId,
            @RequestParam UUID transactionId) {
        return ResponseEntity.ok(purchaseService.confirmPurchase(purchaseId, transactionId));
    }

    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<PurchaseResponseDTO> cancelPurchase(@PathVariable UUID purchaseId) {
        return ResponseEntity.ok(purchaseService.cancelPurchase(purchaseId));
    }

    @PostMapping("/{purchaseId}/refund")
    public ResponseEntity<PurchaseResponseDTO> refundPurchase(@PathVariable UUID purchaseId) {
        return ResponseEntity.ok(purchaseService.refundPurchase(purchaseId));
    }
}
