package com.feitu.dto;

import jakarta.validation.constraints.Size;

public record TarefaDescricaoRequest(
        @Size(max = 4000) String descricao
) {}
