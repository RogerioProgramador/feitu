package com.feitu.dto;

import com.feitu.domain.Usuario;

import java.time.LocalTime;
import java.util.UUID;

public record UsuarioResponse(UUID id, String email, LocalTime horarioNotificacao) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getHorarioNotificacao());
    }
}
