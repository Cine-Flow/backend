package com.android.cineflow.controller;

import com.android.cineflow.dto.request.ForgotPasswordRequest;
import com.android.cineflow.dto.request.LoginRequest;
import com.android.cineflow.dto.request.RegisterRequest;
import com.android.cineflow.dto.request.ResetPasswordRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.LoginResponse;
import com.android.cineflow.service.auth.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
        boolean isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token validation result", isValid));
    }
}
