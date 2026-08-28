package com.ecohome.api.security;

import com.ecohome.api.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userPrincipalUtil")
public class UserPrincipalUtil {

    private final UsuarioRepository usuarioRepository;

    public UserPrincipalUtil(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Integer getId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return -1;
        return usuarioRepository.findByEmail(authentication.getName())
                .map(u -> u.getId())
                .orElse(-1);
    }
}
