package com.feitu.dto;

import com.feitu.domain.TipoTarefa;

import java.util.UUID;

public record TarefaItemResponse(
        UUID id,
        String nome,
        boolean concluida,
        TipoTarefa tipo
) {}
