package com.feitu.dto;

import java.util.List;

public record TarefaRequest(
        String nome,
        String descricao,
        String tipo,
        List<String> diasSemana,
        String horario
) {}
