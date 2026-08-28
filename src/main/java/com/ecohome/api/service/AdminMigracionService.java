package com.ecohome.api.service;

import com.ecohome.api.model.Usuario;
import com.ecohome.api.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminMigracionService {

    private static final Logger log = LoggerFactory.getLogger(AdminMigracionService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SHA256_1234 =
            "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
    private static final String PLACEHOLDER = "$2a$10$PLACEHOLDER_TEMPORAL_CAMBIAR";

    public AdminMigracionService(UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void migrarPasswordsLegacy() {
        for (Usuario u : usuarioRepository.findAll()) {
            String pw = u.getPassword();
            if (pw.startsWith("$2a$") || pw.startsWith("$2b$")) continue;

            String nuevaPassword;
            if ("1234".equals(pw) || SHA256_1234.equals(pw)) {
                nuevaPassword = "1234";
                log.warn("Migrando password legacy de {} a BCrypt. Cambia la contrasena despues del primer login.",
                        u.getEmail());
            } else if (PLACEHOLDER.equals(pw)) {
                nuevaPassword = "EcoHome2026!";
                log.warn("Password placeholder detectada para {}. Asignada contrasena temporal — cambiarla de inmediato.",
                        u.getEmail());
            } else {
                log.error("Password desconocida no migrada para {}. Formato no reconocido.", u.getEmail());
                continue;
            }

            u.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(u);
        }
    }
}
