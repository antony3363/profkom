package com.example.check_in_service.controllers;

import com.example.check_in_service.exceptions.UnauthorizedException;
import com.example.check_in_service.qr.QrCodeImageGenerator;
import com.example.check_in_service.qr.QrTokenService;
import com.example.check_in_service.qr.SegmentQrService;
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
    private final SegmentQrService segmentQrService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMyQr(
            @RequestHeader(value = "X-Person-Id", required = false) Long personId) {
        if (personId == null) {
            throw new UnauthorizedException("Missing X-Person-Id header");
        }
        return ResponseEntity.ok(Map.of("payload", qrTokenService.generatePersonPayload(personId)));
    }

    @GetMapping("/segments/{segmentId}")
    public ResponseEntity<Map<String, String>> getSegmentQr(@PathVariable UUID segmentId) {
        return ResponseEntity.ok(Map.of("payload", segmentQrService.generateSegmentPayload(segmentId)));
    }

//    @GetMapping(value = "/segments/{segmentId}/image", produces = MediaType.IMAGE_PNG_VALUE)
//    public ResponseEntity<byte[]> getSegmentQrImage(@PathVariable UUID segmentId) throws Exception {
//        String payload = segmentQrService.generateSegmentPayload(segmentId);
//        byte[] png = QrCodeImageGenerator.generatePng(payload, 300);
//        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
//    }
}
