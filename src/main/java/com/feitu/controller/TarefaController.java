package com.feitu.controller;

import com.feitu.dto.RenomearRequest;
import com.feitu.dto.TarefaDescricaoRequest;
import com.feitu.dto.TarefaRequest;
import com.feitu.dto.TarefaResponse;
import com.feitu.repository.UsuarioRepository;
import com.feitu.service.SegmentoTempoService;
import com.feitu.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TarefaController {

    private final TarefaService tarefaService;
    private final SegmentoTempoService segmentoTempoService;
    private final UsuarioRepository usuarioRepository;

    public TarefaController(
            TarefaService tarefaService,
            SegmentoTempoService segmentoTempoService,
            UsuarioRepository usuarioRepository) {
        this.tarefaService = tarefaService;
        this.segmentoTempoService = segmentoTempoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/api/workspaces/{workspaceId}/tarefas")
    public List<TarefaResponse> listar(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails principal) {
        UUID uid = usuarioId(principal);
        return tarefaService.listar(workspaceId, uid).stream()
                .map(t -> TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId())))
                .toList();
    }

    @PostMapping("/api/workspaces/{workspaceId}/tarefas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody TarefaRequest req) {
        var t = tarefaService.criar(workspaceId, usuarioId(principal), req);
        return TarefaResponse.from(t, 0L);
    }

    @PatchMapping("/api/tarefas/{id}/descricao")
    public TarefaResponse atualizarDescricao(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid TarefaDescricaoRequest req) {
        var t = tarefaService.atualizarDescricao(id, usuarioId(principal), req.descricao());
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @PatchMapping("/api/tarefas/{id}/nome")
    public TarefaResponse renomear(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid RenomearRequest req) {
        var t = tarefaService.renomear(id, usuarioId(principal), req.nome());
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @PostMapping("/api/tarefas/{id}/iniciar")
    public TarefaResponse iniciar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        var t = tarefaService.iniciarTimer(id, usuarioId(principal));
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @PostMapping("/api/tarefas/{id}/pausar")
    public TarefaResponse pausar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        var t = tarefaService.pausarTimer(id, usuarioId(principal));
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @PostMapping("/api/tarefas/{id}/parar")
    public TarefaResponse parar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        var t = tarefaService.pararTimer(id, usuarioId(principal));
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @PostMapping("/api/tarefas/{id}/reativar")
    public TarefaResponse reativar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        var t = tarefaService.reativar(id, usuarioId(principal));
        return TarefaResponse.from(t, segmentoTempoService.calcularTempoTotalSegundos(t.getId()));
    }

    @DeleteMapping("/api/tarefas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        tarefaService.deletar(id, usuarioId(principal));
    }

    private UUID usuarioId(UserDetails principal) {
        return usuarioRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
    }
}
