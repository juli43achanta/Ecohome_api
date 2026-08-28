package com.ecohome.api.service;

import com.ecohome.api.dto.LoginRequest;
import com.ecohome.api.dto.LoginResponse;
import com.ecohome.api.dto.RegistroRequest;
import com.ecohome.api.model.Usuario;
import com.ecohome.api.repository.UsuarioRepository;
import com.ecohome.api.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager, JwtUtil jwtUtil,
                       UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public LoginResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        String token = jwtUtil.generarToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(req.email()).orElseThrow();
        return new LoginResponse(token, usuario.getNombre(), usuario.getEmail(),
                usuario.getRol().name(), usuario.getId());
    }

    public void registrar(RegistroRequest req) {
        if (!Boolean.TRUE.equals(req.consentimientoGdpr())) {
            throw new IllegalArgumentException("Se requiere aceptar la política de privacidad");
        }
        if (usuarioRepository.existsByEmail(req.email())) {
            // No revelamos si el email existe (email enumeration prevention)
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setEmail(req.email());
        usuario.setPassword(passwordEncoder.encode(req.password()));
        usuario.setRol(Usuario.Rol.USER);
        usuario.setConsentimientoGdpr(true);
        usuarioRepository.save(usuario);
    }
}
