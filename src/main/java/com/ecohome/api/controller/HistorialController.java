package com.ecohome.api.controller;

import com.ecohome.api.model.HistorialActividad;
import com.ecohome.api.repository.HistorialRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialRepository historialRepository;

    public HistorialController(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#usuarioId)")
    public ResponseEntity<List<HistorialActividad>> listar(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(
                historialRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId));
    }

    @DeleteMapping("/usuario/{usuarioId}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#usuarioId)")
    public ResponseEntity<Void> limpiar(@PathVariable Integer usuarioId) {
        historialRepository.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
