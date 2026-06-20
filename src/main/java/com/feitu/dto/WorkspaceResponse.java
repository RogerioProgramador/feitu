package com.feitu.dto;

import com.feitu.domain.Workspace;

import java.util.UUID;

public record WorkspaceResponse(UUID id, String nome, String cor, int ordem) {

    public static WorkspaceResponse from(Workspace ws) {
        return new WorkspaceResponse(ws.getId(), ws.getNome(), ws.getCor(), ws.getOrdem());
    }
}
