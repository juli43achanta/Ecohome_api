package com.ecohome.api.controller;

import com.ecohome.api.dto.DispositivoDTO;
import com.ecohome.api.service.DispositivoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    public DispositivoController(DispositivoService dispositivoService) {
        this.dispositivoService = dispositivoService;
    }

    // Solo el propio usuario o ADMIN puede ver sus dispositivos
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#usuarioId)")
    public ResponseEntity<List<DispositivoDTO>> listar(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(dispositivoService.listarPorUsuario(usuarioId));
    }

    // Solo el propio usuario o ADMIN puede crear dispositivos bajo su cuenta
    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#usuarioId)")
    public ResponseEntity<DispositivoDTO> crear(@PathVariable Integer usuarioId,
                                                 @Valid @RequestBody DispositivoDTO dto) {
        return ResponseEntity.ok(dispositivoService.crear(dto, usuarioId));
    }

    // Solo el propietario o ADMIN puede modificar
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @dispositivoSecurity.esOwner(#id, authentication)")
    public ResponseEntity<DispositivoDTO> modificar(@PathVariable Integer id,
                                                     @Valid @RequestBody DispositivoDTO dto) {
        return ResponseEntity.ok(dispositivoService.modificar(id, dto));
    }

    // Solo el propietario o ADMIN puede eliminar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @dispositivoSecurity.esOwner(#id, authentication)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        dispositivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Solo el propietario o ADMIN puede encender/apagar
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN') or @dispositivoSecurity.esOwner(#id, authentication)")
    public ResponseEntity<DispositivoDTO> toggle(@PathVariable Integer id) {
        return ResponseEntity.ok(dispositivoService.toggleEstado(id));
    }

    // Solo el propietario o ADMIN puede cambiar el valor
    @PostMapping("/{id}/valor")
    @PreAuthorize("hasRole('ADMIN') or @dispositivoSecurity.esOwner(#id, authentication)")
    public ResponseEntity<DispositivoDTO> setValor(@PathVariable Integer id,
                                                    @RequestParam float valor) {
        return ResponseEntity.ok(dispositivoService.setValor(id, valor));
    }
}
