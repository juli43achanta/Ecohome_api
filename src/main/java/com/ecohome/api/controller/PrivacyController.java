package com.ecohome.api.controller;

import com.ecohome.api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PrivacyController {

    private final UsuarioRepository usuarioRepository;

    public PrivacyController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * RGPD Art. 13 — Información al interesado.
     * Endpoint público: los usuarios pueden consultarlo antes de registrarse.
     */
    @GetMapping("/privacy")
    public ResponseEntity<Map<String, Object>> politicaPrivacidad() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("responsable", Map.of(
                "nombre", "EcoHome TFG — Julian Blanco Elvira",
                "contacto", "julianblancoelvira@gmail.com"
        ));
        info.put("finalidad", List.of(
                "Gestión de la cuenta y autenticación del usuario",
                "Control y automatización de dispositivos domóticos del propio usuario",
                "Registro de actividad de los dispositivos para trazabilidad"
        ));
        info.put("base_legal", "Consentimiento explícito del interesado (Art. 6.1.a RGPD)");
        info.put("datos_tratados", List.of("Nombre", "Correo electrónico", "Contraseña (hash BCrypt)", "Historial de actividad de dispositivos propios"));
        info.put("conservacion", "Los datos se conservan mientras la cuenta esté activa. Al eliminar la cuenta se borran todos los datos asociados.");
        info.put("destinatarios", "No se ceden datos a terceros. El sistema se aloja en infraestructura propia o en proveedores europeos con SCCs.");
        info.put("derechos", Map.of(
                "acceso", "GET /api/usuarios/{id}",
                "supresion", "DELETE /api/usuarios/{id}/datos",
                "portabilidad", "Contactar con el responsable",
                "rectificacion", "Contactar con el responsable"
        ));
        info.put("autoridad_control", "Agencia Española de Protección de Datos (AEPD) — www.aepd.es");
        return ResponseEntity.ok(info);
    }

    /**
     * RGPD Art. 17 — Derecho al olvido.
     * Elimina el usuario y TODOS sus datos (historial, dispositivos).
     * El FK ON DELETE CASCADE en BD garantiza la eliminación en cascada.
     */
    @DeleteMapping("/usuarios/{id}/datos")
    @PreAuthorize("hasRole('ADMIN') or @userPrincipalUtil.getId(authentication).equals(#id)")
    public ResponseEntity<Map<String, String>> eliminarTodosDatos(@PathVariable Integer id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Todos los datos del usuario han sido eliminados permanentemente (RGPD Art. 17)"
        ));
    }
}
