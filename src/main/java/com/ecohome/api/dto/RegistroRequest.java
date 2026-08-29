package com.ecohome.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).+",
                 message = "La contraseña debe contener al menos una letra y un número")
        String password,
        @AssertTrue(message = "Debes aceptar la política de privacidad para registrarte") Boolean consentimientoGdpr
) {}
