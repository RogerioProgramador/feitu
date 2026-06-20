package com.feitu.controller;

import com.feitu.dto.SubscribeRequest;
import com.feitu.dto.UnsubscribeRequest;
import com.feitu.repository.UsuarioRepository;
import com.feitu.service.PushSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final PushSubscriptionService pushSubscriptionService;
    private final UsuarioRepository usuarioRepository;

    public PushController(PushSubscriptionService pushSubscriptionService, UsuarioRepository usuarioRepository) {
        this.pushSubscriptionService = pushSubscriptionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid SubscribeRequest req) {
        pushSubscriptionService.subscribe(usuarioId(principal), req);
    }

    @DeleteMapping("/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid UnsubscribeRequest req) {
        pushSubscriptionService.unsubscribe(usuarioId(principal), req.endpoint());
    }

    private UUID usuarioId(UserDetails principal) {
        return usuarioRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
    }
}
