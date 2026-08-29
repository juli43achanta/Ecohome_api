package com.ecohome.api.security;

import com.ecohome.api.model.Dispositivo;
import com.ecohome.api.model.Usuario;
import com.ecohome.api.repository.DispositivoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DispositivoSecurityTest {

    @Test
    void permiteAccesoSoloAlPropietarioDelDispositivo() {
        DispositivoRepository repo = mock(DispositivoRepository.class);
        DispositivoSecurity security = new DispositivoSecurity(repo);

        Usuario propietario = new Usuario();
        propietario.setEmail("dueno@ecohome.com");
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setUsuario(propietario);

        when(repo.findById(10)).thenReturn(Optional.of(dispositivo));

        Authentication autenticadoComoOtro = mock(Authentication.class);
        when(autenticadoComoOtro.getName()).thenReturn("otro@ecohome.com");
        assertThat(security.esOwner(10, autenticadoComoOtro)).isFalse();

        Authentication autenticadoComoDueno = mock(Authentication.class);
        when(autenticadoComoDueno.getName()).thenReturn("dueno@ecohome.com");
        assertThat(security.esOwner(10, autenticadoComoDueno)).isTrue();
    }

    @Test
    void dispositivoInexistenteNiegaElAcceso() {
        DispositivoRepository repo = mock(DispositivoRepository.class);
        DispositivoSecurity security = new DispositivoSecurity(repo);
        when(repo.findById(99)).thenReturn(Optional.empty());

        Authentication auth = mock(Authentication.class);
        assertThat(security.esOwner(99, auth)).isFalse();
    }
}
