package com.android.cineflow.service.email;

public interface IEmailService {
    void sendPasswordResetEmail(String toEmail, String token, String username);
    void sendWelcomeEmail(String toEmail, String username);
}
