package com.feitu.security;

import com.feitu.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthUtils {

    private final UsuarioRepository usuarioRepository;

    public AuthUtils(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UUID usuarioId(UserDetails principal) {
        return usuarioRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
    }
}
