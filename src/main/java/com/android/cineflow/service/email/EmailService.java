package com.android.cineflow.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@cineflow.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("CineFlow - Password Reset Request");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetEmailContent(username, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to CineFlow!");

            String htmlContent = buildWelcomeEmailContent(username);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailContent(String username, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎬 CineFlow</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password for your CineFlow account.</p>
                        <p>Click the button below to reset your password:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>This link will expire in 30 minutes.</p>
                        <p>If you didn't request a password reset, you can safely ignore this email.</p>
                        <p>Best regards,<br>The CineFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 CineFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, resetLink);
    }

    private String buildWelcomeEmailContent(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎬 CineFlow</h1>
                    </div>
                    <div class="content">
                        <h2>Welcome to CineFlow, %s! 🎉</h2>
                        <p>Thank you for joining CineFlow - your ultimate movie streaming destination.</p>
                        <p>With your new account, you can:</p>
                        <ul>
                            <li>🎬 Stream thousands of movies and TV shows</li>
                            <li>❤️ Create your personalized favorites list</li>
                            <li>📺 Continue watching from where you left off</li>
                            <li>🔔 Get notifications about new releases</li>
                        </ul>
                        <p>Start exploring now and enjoy unlimited entertainment!</p>
                        <p>Best regards,<br>The CineFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 CineFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);
    }
}
