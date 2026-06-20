package com.feitu.service;

import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.Usuario;
import com.feitu.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario buscar(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public Usuario atualizarHorarioNotificacao(UUID id, LocalTime horario) {
        Usuario u = buscar(id);
        u.setHorarioNotificacao(horario);
        return usuarioRepository.save(u);
    }
}
