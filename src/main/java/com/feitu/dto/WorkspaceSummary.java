package com.feitu.dto;

import java.util.UUID;

public record WorkspaceSummary(UUID workspaceId, String nome, String cor, long segundos) {}
