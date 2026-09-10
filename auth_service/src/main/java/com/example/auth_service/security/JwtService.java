package com.example.auth_service.security;

import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exceptions.InvalidTokenException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ручная реализация JWT (RS256) без сторонней библиотеки — формат стандартный
 * (header.payload.signature, base64url), проверяется/выпускается только этим
 * сервисом. API Gateway для валидации по публичному ключу здесь ещё не
 * реализован — этого сервиса-шлюза пока нет в репозитории.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER_JSON = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";

    private final JwtKeyProvider keyProvider;

    @Value("${auth.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    public String issueAccessToken(long lichnostId, UserRole role, Long schoolId) {
        long now = Instant.now().getEpochSecond();
        long exp = now + accessTokenTtlMinutes * 60;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(lichnostId));
        payload.put("role", role.name());
        if (schoolId != null) {
            payload.put("schoolId", schoolId);
        }
        payload.put("iat", now);
        payload.put("exp", exp);

        return encode(payload);
    }

    public AccessTokenClaims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Missing token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("Malformed JWT");
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] signature = DECODER.decode(parts[2]);

        if (!verifySignature(signingInput, signature)) {
            throw new InvalidTokenException("Invalid JWT signature");
        }

        Map<String, Object> payload;
        try {
            payload = MAPPER.readValue(DECODER.decode(parts[1]), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new InvalidTokenException("Malformed JWT payload");
        }

        long exp = ((Number) payload.get("exp")).longValue();
        if (Instant.now().getEpochSecond() > exp) {
            throw new InvalidTokenException("Token expired");
        }

        long lichnostId = Long.parseLong((String) payload.get("sub"));
        UserRole role = UserRole.valueOf((String) payload.get("role"));
        Long schoolId = payload.get("schoolId") != null ? ((Number) payload.get("schoolId")).longValue() : null;
        return new AccessTokenClaims(lichnostId, role, schoolId, exp);
    }

    private String encode(Map<String, Object> payload) {
        try {
            String headerPart = ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
            String payloadPart = ENCODER.encodeToString(MAPPER.writeValueAsBytes(payload));
            String signingInput = headerPart + "." + payloadPart;

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign((PrivateKey) keyProvider.getKeyPair().getPrivate());
            signer.update(signingInput.getBytes(StandardCharsets.UTF_8));
            String signaturePart = ENCODER.encodeToString(signer.sign());

            return signingInput + "." + signaturePart;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    private boolean verifySignature(String signingInput, byte[] signature) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify((PublicKey) keyProvider.getKeyPair().getPublic());
            verifier.update(signingInput.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
