package com.ecohome.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        String currentPassword,
        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).+",
                 message = "La contraseña debe contener al menos una letra y un número")
        String newPassword
) {}
