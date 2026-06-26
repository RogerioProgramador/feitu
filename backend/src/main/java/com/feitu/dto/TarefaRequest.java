package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TarefaRequest(
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 4000) String descricao,
        String tipo,
        List<String> diasSemana,
        String horario
) {}
