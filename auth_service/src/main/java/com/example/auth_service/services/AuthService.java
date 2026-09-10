package com.example.auth_service.services;

import com.example.auth_service.DTOs.TokenPairResponseDTO;
import com.example.auth_service.entities.AppUser;
import com.example.auth_service.entities.RefreshToken;
import com.example.auth_service.exceptions.InvalidTokenException;
import com.example.auth_service.repositories.RefreshTokenRepository;
import com.example.auth_service.security.JwtService;
import com.example.auth_service.security.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Value("${auth.jwt.refresh-token-ttl-days}")
    private long refreshTokenTtlDays;

    @Transactional
    public TokenPairResponseDTO login(long lichnostId, String email, String firstName, String lastName) {
        AppUser user = userService.getOrCreateUser(lichnostId, email, firstName, lastName);
        return issueTokenPair(user);
    }

    @Transactional
    public TokenPairResponseDTO refresh(String rawRefreshToken) {
        String hash = refreshTokenGenerator.hash(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Unknown refresh token"));

        if (existing.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        // ротация: старый токен отзывается, выпускается новый
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return issueTokenPair(existing.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = refreshTokenGenerator.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private TokenPairResponseDTO issueTokenPair(AppUser user) {
        String accessToken = jwtService.issueAccessToken(user.getLichnostId(), user.getRole(), user.getSchoolId());

        String rawRefreshToken = refreshTokenGenerator.generateRawToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenGenerator.hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenTtlDays))
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenPairResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresInSeconds(accessTokenTtlMinutes * 60)
                .build();
    }
}
