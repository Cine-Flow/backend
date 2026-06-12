package com.android.cineflow.service.auth;

import com.android.cineflow.dto.request.ForgotPasswordRequest;
import com.android.cineflow.dto.request.LoginRequest;
import com.android.cineflow.dto.request.RegisterRequest;
import com.android.cineflow.dto.request.ResetPasswordRequest;
import com.android.cineflow.dto.response.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateResetToken(String token);

    LoginResponse refreshToken(com.android.cineflow.dto.request.TokenRefreshRequest request);

    void logout(String refreshToken);
}

