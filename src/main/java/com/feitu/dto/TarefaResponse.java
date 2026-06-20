package com.feitu.dto;

import com.feitu.domain.Tarefa;
import com.feitu.domain.TarefaEstado;

import java.time.LocalDateTime;
import java.util.UUID;

public record TarefaResponse(
        UUID id,
        String nome,
        TarefaEstado estado,
        long tempoTotalSegundos,
        LocalDateTime criadoEm,
        LocalDateTime concluidoEm
) {
    public static TarefaResponse from(Tarefa t, long tempoTotalSegundos) {
        return new TarefaResponse(
                t.getId(), t.getNome(), t.getEstado(),
                tempoTotalSegundos, t.getCriadoEm(), t.getConcluidoEm()
        );
    }
}
