package com.android.cineflow.service.auth;

import com.android.cineflow.exceptions.TokenRefreshException;
import com.android.cineflow.model.RefreshToken;
import com.android.cineflow.model.User;
import com.android.cineflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.token.refreshExpirationInDays:7}")
    private Long refreshExpirationInDays;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.info("Creating refresh token for user: {}", user.getEmail());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(refreshExpirationInDays, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expired and deleted: {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please sign in again.");
        }
        return token;
    }

    @Transactional
    public void revokeAllByUserId(String userId) {
        log.info("Revoking all refresh tokens for user ID: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Rotates the refresh token (creates a new one and revokes the old one).
     * Also detects if an old/revoked token is reuse (indicates token theft).
     */
    @Transactional
    public RefreshToken rotateToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new TokenRefreshException(tokenStr, "Refresh token is not database."));

        // Theft Detection: If token is already revoked, it means it was used before.
        // This suggests that a malicious attacker might have stolen the token and is trying to reuse it,
        // or the legitimate user is using a stale token.
        // Action: Revoke ALL tokens belonging to this user to force re-authentication.
        if (token.isRevoked()) {
            log.warn("Detecting refresh token reuse! Revoking all tokens for user: {}", token.getUser().getEmail());
            refreshTokenRepository.revokeAllByUserId(token.getUser().getId());
            throw new TokenRefreshException(tokenStr, "This refresh token has already been used. Security violation detected! All sessions revoked.");
        }

        // Verify if token is expired
        verifyExpiration(token);

        // Revoke the old token
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // Create and return a new one
        return createRefreshToken(token.getUser());
    }

    /**
     * Daily cleanup task at midnight to purge expired tokens from database.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens.");
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Scheduled cleanup completed.");
    }
}
