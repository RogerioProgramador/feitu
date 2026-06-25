package com.feitu.controller;

import com.feitu.dto.DailySummaryResponse;
import com.feitu.security.AuthUtils;
import com.feitu.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthUtils authUtils;

    public AnalyticsController(AnalyticsService analyticsService, AuthUtils authUtils) {
        this.analyticsService = analyticsService;
        this.authUtils = authUtils;
    }

    @GetMapping("/daily")
    public DailySummaryResponse sumarioDiario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate data = date != null ? date : LocalDate.now();
        return analyticsService.sumarioDiario(authUtils.usuarioId(principal), data);
    }
}
