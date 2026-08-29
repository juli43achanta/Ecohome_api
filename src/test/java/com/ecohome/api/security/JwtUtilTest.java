package com.ecohome.api.security;

import com.ecohome.api.model.Usuario;
import com.ecohome.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private static final String SECRET = "TestSecretKeyParaJwtUtilTest1234567890ABCDEF";

    private UsuarioRepository usuarioRepository;
    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        jwtUtil = new JwtUtil(usuarioRepository);
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600_000L);
        ReflectionTestUtils.setField(jwtUtil, "perfilActivo", "dev");
        jwtUtil.validarSecreto();

        userDetails = new User("julian@ecohome.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private Usuario usuarioConVersion(int version) {
        Usuario u = new Usuario();
        u.setEmail("julian@ecohome.com");
        u.setTokenVersion(version);
        return u;
    }

    @Test
    void tokenRecienGeneradoEsValidoParaSuVersionActual() {
        when(usuarioRepository.findByEmail("julian@ecohome.com"))
                .thenReturn(Optional.of(usuarioConVersion(0)));

        String token = jwtUtil.generarToken(userDetails, 0);

        assertThat(jwtUtil.esValido(token, userDetails)).isTrue();
        assertThat(jwtUtil.extraerEmail(token)).isEqualTo("julian@ecohome.com");
    }

    @Test
    void tokenSeInvalidaTrasCambiarPasswordOCerrarSesiones() {
        // El token se firmó con tokenVersion=0, pero en BD ya está en 1
        // (p.ej. tras un cambio de contraseña o "cerrar sesiones activas")
        String token = jwtUtil.generarToken(userDetails, 0);

        when(usuarioRepository.findByEmail("julian@ecohome.com"))
                .thenReturn(Optional.of(usuarioConVersion(1)));

        assertThat(jwtUtil.esValido(token, userDetails)).isFalse();
    }

    @Test
    void tokenDeUsuarioEliminadoNoEsValido() {
        String token = jwtUtil.generarToken(userDetails, 0);

        when(usuarioRepository.findByEmail("julian@ecohome.com"))
                .thenReturn(Optional.empty());

        assertThat(jwtUtil.esValido(token, userDetails)).isFalse();
    }

    @Test
    void tokenManipuladoNoSuperaLaVerificacionDeFirma() {
        String token = jwtUtil.generarToken(userDetails, 0);
        String tokenManipulado = token.substring(0, token.length() - 4) + "abcd";

        assertThat(jwtUtil.esValido(tokenManipulado, userDetails)).isFalse();
    }

    @Test
    void secretoDeDesarrolloNoRompeElArranqueFueraDeProduccion() {
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "EcoHomeDevSecretKey2026ParaDesarrolloLocalUnicamente!!");
        ReflectionTestUtils.setField(jwtUtil, "perfilActivo", "dev");

        jwtUtil.validarSecreto(); // no debe lanzar excepción
    }

    @Test
    void secretoDeDesarrolloImpideElArranqueEnProduccion() {
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "EcoHomeDevSecretKey2026ParaDesarrolloLocalUnicamente!!");
        ReflectionTestUtils.setField(jwtUtil, "perfilActivo", "prod");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                jwtUtil::validarSecreto);
    }
}
