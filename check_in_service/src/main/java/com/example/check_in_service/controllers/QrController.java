package com.example.check_in_service.controllers;

import com.example.check_in_service.exceptions.UnauthorizedException;
import com.example.check_in_service.qr.QrCodeImageGenerator;
import com.example.check_in_service.qr.QrTokenService;
import com.example.check_in_service.qr.EventQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrTokenService qrTokenService;
    private final EventQrService eventQrService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMyQr(
            @RequestHeader(value = "X-Lichnost-Id", required = false) Long lichnostId) {
        if (lichnostId == null) {
            throw new UnauthorizedException("Missing X-Lichnost-Id header");
        }
        return ResponseEntity.ok(Map.of("payload", qrTokenService.generatePersonPayload(lichnostId)));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<Map<String, String>> getEventQr(@PathVariable UUID eventId) {
        return ResponseEntity.ok(Map.of("payload", eventQrService.generateEventPayload(eventId)));
    }

//    @GetMapping(value = "/events/{eventId}/image", produces = MediaType.IMAGE_PNG_VALUE)
//    public ResponseEntity<byte[]> getEventQrImage(@PathVariable UUID eventId) throws Exception {
//        String payload = eventQrService.generateEventPayload(eventId);
//        byte[] png = QrCodeImageGenerator.generatePng(payload, 300);
//        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
//    }
}
