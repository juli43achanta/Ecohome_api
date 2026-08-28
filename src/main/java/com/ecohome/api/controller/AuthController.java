package com.ecohome.api.controller;

import com.ecohome.api.dto.LoginRequest;
import com.ecohome.api.dto.LoginResponse;
import com.ecohome.api.dto.RegistroRequest;
import com.ecohome.api.security.LoginRateLimiter;
import com.ecohome.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest httpRequest) {
        String ip = obtenerIp(httpRequest);
        if (!rateLimiter.permitido(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Demasiados intentos de login. Espera 1 minuto."));
        }
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@Valid @RequestBody RegistroRequest req) {
        authService.registrar(req);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    // Con server.forward-headers-strategy=framework, Spring resuelve
    // X-Forwarded-For del proxy de confianza antes de que llegue aquí.
    // getRemoteAddr() ya devuelve la IP real del cliente.
    private String obtenerIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
