package com.ecohome.api.service;

import com.ecohome.api.dto.LoginRequest;
import com.ecohome.api.dto.LoginResponse;
import com.ecohome.api.dto.RegistroRequest;
import com.ecohome.api.model.Usuario;
import com.ecohome.api.repository.UsuarioRepository;
import com.ecohome.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authManager;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authManager = mock(AuthenticationManager.class);
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(UserDetailsService.class);
        authService = new AuthService(usuarioRepository, passwordEncoder, authManager, jwtUtil, userDetailsService);
    }

    private Usuario usuarioBase() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setNombre("Julian");
        u.setEmail("julian@ecohome.com");
        u.setPassword("hash-actual");
        u.setRol(Usuario.Rol.USER);
        u.setTokenVersion(3);
        return u;
    }

    @Test
    void loginDevuelveTokenConLaVersionActualDelUsuario() {
        Usuario usuario = usuarioBase();
        UserDetails userDetails = new User("julian@ecohome.com", "hash-actual",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(usuarioRepository.findByEmail("julian@ecohome.com")).thenReturn(Optional.of(usuario));
        when(userDetailsService.loadUserByUsername("julian@ecohome.com")).thenReturn(userDetails);
        when(jwtUtil.generarToken(userDetails, 3)).thenReturn("token-generado");

        LoginResponse resp = authService.login(new LoginRequest("julian@ecohome.com", "clave1234"));

        assertThat(resp.token()).isEqualTo("token-generado");
        assertThat(resp.userId()).isEqualTo(1);
        verify(authManager).authenticate(any());
    }

    @Test
    void registrarSinConsentimientoGdprLanzaExcepcion() {
        RegistroRequest req = new RegistroRequest("Julian", "julian@ecohome.com", "clave1234", false);

        assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void registrarConEmailExistenteNoGuardaNiLanzaExcepcion_preveniendoEnumeracion() {
        RegistroRequest req = new RegistroRequest("Julian", "julian@ecohome.com", "clave1234", true);
        when(usuarioRepository.existsByEmail("julian@ecohome.com")).thenReturn(true);

        authService.registrar(req);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarConEmailNuevoGuardaConPasswordCifrada() {
        RegistroRequest req = new RegistroRequest("Julian", "nuevo@ecohome.com", "clave1234", true);
        when(usuarioRepository.existsByEmail("nuevo@ecohome.com")).thenReturn(false);
        when(passwordEncoder.encode("clave1234")).thenReturn("hash-bcrypt");

        authService.registrar(req);

        verify(usuarioRepository).save(argThat(u ->
                u.getEmail().equals("nuevo@ecohome.com")
                        && u.getPassword().equals("hash-bcrypt")
                        && u.getRol() == Usuario.Rol.USER
                        && Boolean.TRUE.equals(u.getConsentimientoGdpr())));
    }

    @Test
    void cambiarPasswordConCurrentPasswordIncorrectaLanzaExcepcionYNoGuarda() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash-actual")).thenReturn(false);

        assertThatThrownBy(() -> authService.cambiarPassword(1, "mala", "nuevaClave1", false))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPasswordConCurrentPasswordCorrectaActualizaEIncrementaTokenVersion() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("buena", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nuevaClave1")).thenReturn("hash-nuevo");

        authService.cambiarPassword(1, "buena", "nuevaClave1", false);

        assertThat(usuario.getPassword()).isEqualTo("hash-nuevo");
        assertThat(usuario.getTokenVersion()).isEqualTo(4);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void resetDeAdminNoRequiereCurrentPassword() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("otraClave1")).thenReturn("hash-reset");

        authService.cambiarPassword(1, null, "otraClave1", true);

        assertThat(usuario.getPassword()).isEqualTo("hash-reset");
        assertThat(usuario.getTokenVersion()).isEqualTo(4);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void cerrarSesionesActivasIncrementaTokenVersionSinTocarLaPassword() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        authService.cerrarSesionesActivas(1);

        assertThat(usuario.getTokenVersion()).isEqualTo(4);
        assertThat(usuario.getPassword()).isEqualTo("hash-actual");
        verify(usuarioRepository).save(usuario);
    }
}
