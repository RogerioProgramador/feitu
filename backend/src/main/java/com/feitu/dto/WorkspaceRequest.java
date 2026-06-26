package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
        @NotBlank @Size(max = 100) String nome,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve ser um hex válido (#RRGGBB)")
        String cor
) {}
