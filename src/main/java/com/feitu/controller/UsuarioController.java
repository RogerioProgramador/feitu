package com.feitu.controller;

import com.feitu.dto.NotificacaoConfigRequest;
import com.feitu.dto.UsuarioResponse;
import com.feitu.security.AuthUtils;
import com.feitu.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthUtils authUtils;

    public UsuarioController(UsuarioService usuarioService, AuthUtils authUtils) {
        this.usuarioService = usuarioService;
        this.authUtils = authUtils;
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UserDetails principal) {
        return UsuarioResponse.from(usuarioService.buscar(usuarioId(principal)));
    }

    @PutMapping("/me/notificacao")
    public UsuarioResponse atualizarNotificacao(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid NotificacaoConfigRequest req) {
        LocalTime horario = LocalTime.parse(req.horario());
        return UsuarioResponse.from(usuarioService.atualizarHorarioNotificacao(usuarioId(principal), horario));
    }

    private UUID usuarioId(UserDetails principal) {
        return authUtils.usuarioId(principal);
    }
}
