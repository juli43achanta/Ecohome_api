package com.ecohome.api.security;

import com.ecohome.api.repository.DispositivoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("dispositivoSecurity")
public class DispositivoSecurity {

    private final DispositivoRepository dispositivoRepository;

    public DispositivoSecurity(DispositivoRepository dispositivoRepository) {
        this.dispositivoRepository = dispositivoRepository;
    }

    public boolean esOwner(Integer dispositivoId, Authentication auth) {
        return dispositivoRepository.findById(dispositivoId)
                .map(d -> d.getUsuario().getEmail().equals(auth.getName()))
                .orElse(false);
    }
}
