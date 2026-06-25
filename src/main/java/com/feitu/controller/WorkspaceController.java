package com.feitu.controller;

import com.feitu.dto.ReorderRequest;
import com.feitu.dto.WorkspaceRequest;
import com.feitu.dto.WorkspaceResponse;
import com.feitu.security.AuthUtils;
import com.feitu.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AuthUtils authUtils;

    public WorkspaceController(WorkspaceService workspaceService, AuthUtils authUtils) {
        this.workspaceService = workspaceService;
        this.authUtils = authUtils;
    }

    @GetMapping
    public List<WorkspaceResponse> listar(@AuthenticationPrincipal UserDetails principal) {
        return workspaceService.listar(usuarioId(principal))
                .stream().map(WorkspaceResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse criar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid WorkspaceRequest req) {
        return WorkspaceResponse.from(workspaceService.criar(usuarioId(principal), req));
    }

    @PutMapping("/{id}")
    public WorkspaceResponse atualizar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid WorkspaceRequest req) {
        return WorkspaceResponse.from(workspaceService.atualizar(id, usuarioId(principal), req));
    }

    @PutMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordenar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ReorderRequest req) {
        workspaceService.reordenar(usuarioId(principal), req.ids());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        workspaceService.deletar(id, usuarioId(principal));
    }

    private UUID usuarioId(UserDetails principal) {
        return authUtils.usuarioId(principal);
    }
}
