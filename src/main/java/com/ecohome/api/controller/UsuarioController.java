package com.ecohome.api.controller;

import com.ecohome.api.dto.CambiarPasswordRequest;
import com.ecohome.api.dto.UsuarioDTO;
import com.ecohome.api.repository.UsuarioRepository;
import com.ecohome.api.security.UserPrincipalUtil;
import com.ecohome.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final UserPrincipalUtil userPrincipalUtil;

    public UsuarioController(UsuarioRepository usuarioRepository, AuthService authService,
                              UserPrincipalUtil userPrincipalUtil) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.userPrincipalUtil = userPrincipalUtil;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        List<UsuarioDTO> dtos = usuarioRepository.findAll()
                .stream().map(UsuarioDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }

    // Solo el propio usuario o un ADMIN puede ver el perfil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#id)")
    public ResponseEntity<UsuarioDTO> get(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioDTO.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Cambia la contraseña propia (requiere currentPassword) o, si el que llama es
    // ADMIN y no es su propia cuenta, la resetea directamente (migración de cuentas legacy).
    // En ambos casos invalida los tokens JWT emitidos previamente.
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#id)")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Integer id,
                                                 @Valid @RequestBody CambiarPasswordRequest req,
                                                 Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean esResetDeAdmin = esAdmin && !userPrincipalUtil.getId(authentication).equals(id);
        authService.cambiarPassword(id, req.currentPassword(), req.newPassword(), esResetDeAdmin);
        return ResponseEntity.noContent().build();
    }

    // Invalida todos los tokens JWT activos del usuario (logout en todos los dispositivos)
    @PostMapping("/{id}/cerrar-sesiones")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#id)")
    public ResponseEntity<Map<String, String>> cerrarSesiones(@PathVariable Integer id) {
        authService.cerrarSesionesActivas(id);
        return ResponseEntity.ok(Map.of("mensaje", "Sesiones activas cerradas"));
    }
}
