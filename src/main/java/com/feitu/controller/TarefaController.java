package com.feitu.controller;

import com.feitu.dto.RenomearRequest;
import com.feitu.dto.TarefaDescricaoRequest;
import com.feitu.dto.TarefaRequest;
import com.feitu.dto.TarefaResponse;
import com.feitu.security.AuthUtils;
import com.feitu.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class TarefaController {

    private final TarefaService tarefaService;
    private final AuthUtils authUtils;

    public TarefaController(TarefaService tarefaService, AuthUtils authUtils) {
        this.tarefaService = tarefaService;
        this.authUtils = authUtils;
    }

    @GetMapping("/api/workspaces/{workspaceId}/tarefas")
    public List<TarefaResponse> listar(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate dia = date != null ? date : LocalDate.now();
        return tarefaService.listarParaDia(workspaceId, usuarioId(principal), dia);
    }

    @PostMapping("/api/workspaces/{workspaceId}/tarefas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody TarefaRequest req) {
        LocalDate dia = date != null ? date : LocalDate.now();
        return tarefaService.criar(workspaceId, usuarioId(principal), req, dia);
    }

    @PatchMapping("/api/tarefas/{id}/descricao")
    public TarefaResponse atualizarDescricao(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid TarefaDescricaoRequest req) {
        return tarefaService.atualizarDescricao(id, usuarioId(principal), req.descricao());
    }

    @PatchMapping("/api/tarefas/{id}/nome")
    public TarefaResponse renomear(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid RenomearRequest req) {
        return tarefaService.renomear(id, usuarioId(principal), req.nome());
    }

    @PostMapping("/api/tarefas/{id}/concluir")
    public TarefaResponse concluir(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate dia = date != null ? date : LocalDate.now();
        return tarefaService.concluir(id, usuarioId(principal), dia);
    }

    @PostMapping("/api/tarefas/{id}/reabrir")
    public TarefaResponse reabrir(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate dia = date != null ? date : LocalDate.now();
        return tarefaService.reabrir(id, usuarioId(principal), dia);
    }

    @DeleteMapping("/api/tarefas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        tarefaService.deletar(id, usuarioId(principal));
    }

    private UUID usuarioId(UserDetails principal) {
        return authUtils.usuarioId(principal);
    }
}
