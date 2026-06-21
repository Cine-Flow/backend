package com.android.cineflow.service.auth;

import com.android.cineflow.dto.request.ForgotPasswordRequest;
import com.android.cineflow.dto.request.LoginRequest;
import com.android.cineflow.dto.request.RegisterRequest;
import com.android.cineflow.dto.request.ResetPasswordRequest;
import com.android.cineflow.dto.response.LoginResponse;
import com.android.cineflow.exceptions.DuplicateResourceException;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.User;
import com.android.cineflow.model.enums.UserRole;
import com.android.cineflow.repository.UserRepository;
import com.android.cineflow.security.jwt.JwtUtils;
import com.android.cineflow.security.userdetails.AppUserDetails;
import com.android.cineflow.service.email.EmailService;
import com.android.cineflow.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final RefreshTokenService refreshTokenService;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateAccessToken(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create refresh token
        com.android.cineflow.model.RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Login successful for user: {}", userDetails.getUsername());

        return new LoginResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("")
        );
    }

    @Override
    public void register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password reset failed: Email not found - {}", request.getEmail());
                    return new ResourceNotFoundException("No account found with this email address");
                });

        // Generate new token and store on user
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);

        // Send email asynchronously
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());

        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String token = normalizeResetToken(request.getToken());

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (!user.isResetTokenValid()) {
            log.warn("Password reset failed: Token expired");
            throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Clear the reset token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        // Force all devices to re-login by revoking all refresh tokens
        refreshTokenService.revokeAllByUserId(user.getId());

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    public boolean validateResetToken(String token) {
        return userRepository.findByResetToken(normalizeResetToken(token))
                .map(User::isResetTokenValid)
                .orElse(false);
    }

    private String normalizeResetToken(String rawToken) {
        if (rawToken == null) {
            return "";
        }

        String token = rawToken.trim();
        int tokenParamIndex = token.indexOf("token=");
        if (tokenParamIndex >= 0) {
            String tokenValue = token.substring(tokenParamIndex + "token=".length());
            int ampersandIndex = tokenValue.indexOf('&');
            return ampersandIndex >= 0 ? tokenValue.substring(0, ampersandIndex).trim() : tokenValue.trim();
        }

        return token;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public LoginResponse refreshToken(com.android.cineflow.dto.request.TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Rotate token (verify, revoke old, create new)
        com.android.cineflow.model.RefreshToken rotatedToken = refreshTokenService.rotateToken(requestRefreshToken);
        User user = rotatedToken.getUser();

        // Generate new access token
        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rotatedToken.getToken())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }
}
