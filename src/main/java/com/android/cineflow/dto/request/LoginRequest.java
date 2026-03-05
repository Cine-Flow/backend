package com.android.cineflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email, username or phone number is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
