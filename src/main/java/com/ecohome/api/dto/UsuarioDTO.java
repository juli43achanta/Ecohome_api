package com.ecohome.api.dto;

import com.ecohome.api.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioDTO(Integer id, String nombre, String email, String rol,
                         Boolean consentimientoGdpr, LocalDateTime fechaRegistro) {
    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(),
                u.getConsentimientoGdpr(), u.getFechaRegistro());
    }
}
