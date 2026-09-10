package com.example.check_in_service.qr;

import com.example.check_in_service.exceptions.InvalidQrPayloadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Generates and verifies the static, non-expiring person QR token: "{lichnostId}.{hmacSignature}".
 * Static by product decision: it never expires and has no revocation mechanism, so a leaked
 * QR stays valid forever - accepted tradeoff for attendance tracking (not payments).
 */
@Service
public class QrTokenService {

    private final String secret;

    public QrTokenService(@Value("${qr.person.secret}") String secret) {
        this.secret = secret;
    }

    public String generatePersonPayload(long lichnostId) {
        String data = String.valueOf(lichnostId);
        return data + "." + sign(data);
    }

    public long verifyAndExtractLichnostId(String payload) {
        if (payload == null) {
            throw new InvalidQrPayloadException("QR payload is missing");
        }

        String[] parts = payload.split("\\.", 2);
        if (parts.length != 2) {
            throw new InvalidQrPayloadException("Malformed person QR payload");
        }

        long lichnostId;
        try {
            lichnostId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new InvalidQrPayloadException("Malformed person QR payload");
        }

        String expectedSignature = sign(parts[0]);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidQrPayloadException("Invalid person QR signature");
        }

        return lichnostId;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to sign QR payload", e);
        }
    }
}
