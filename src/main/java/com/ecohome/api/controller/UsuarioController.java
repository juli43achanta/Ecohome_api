package com.ecohome.api.controller;

import com.ecohome.api.dto.UsuarioDTO;
import com.ecohome.api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}
