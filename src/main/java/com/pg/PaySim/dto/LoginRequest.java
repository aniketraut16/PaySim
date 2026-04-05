package com.pg.PaySim.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
public class LoginRequest {
    
    @NonNull
    @NotEmpty
    @Email(message = "Invalid email address")
    private String email;

    @NonNull
    @NotEmpty
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
}
