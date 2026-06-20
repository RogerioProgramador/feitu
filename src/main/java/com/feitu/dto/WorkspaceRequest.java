package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WorkspaceRequest(
        @NotBlank String nome,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve ser um hex válido (#RRGGBB)")
        String cor
) {}
