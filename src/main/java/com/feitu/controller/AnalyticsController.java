package com.feitu.controller;

import com.feitu.dto.DailySummaryResponse;
import com.feitu.repository.UsuarioRepository;
import com.feitu.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UsuarioRepository usuarioRepository;

    public AnalyticsController(AnalyticsService analyticsService, UsuarioRepository usuarioRepository) {
        this.analyticsService = analyticsService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/daily")
    public DailySummaryResponse sumarioDiario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate data = date != null ? date : LocalDate.now();
        UUID uid = usuarioRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
        return analyticsService.sumarioDiario(uid, data);
    }
}
