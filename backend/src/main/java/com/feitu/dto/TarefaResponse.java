package com.feitu.dto;

import com.feitu.domain.Tarefa;
import com.feitu.domain.TipoTarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record TarefaResponse(
        UUID id,
        String nome,
        TipoTarefa tipo,
        boolean concluida,
        LocalDateTime concluidaEm,
        LocalDate data,
        List<String> diasSemana,
        LocalTime horario,
        LocalDateTime criadoEm,
        String descricao
) {
    public static TarefaResponse from(Tarefa t, boolean concluida) {
        List<String> dias = t.getDiasSemana() != null && !t.getDiasSemana().isBlank()
                ? Arrays.asList(t.getDiasSemana().split(","))
                : List.of();
        return new TarefaResponse(
                t.getId(),
                t.getNome(),
                t.getTipo(),
                concluida,
                t.getConcluidaEm(),
                t.getData(),
                dias,
                t.getHorario(),
                t.getCriadoEm(),
                t.getDescricao()
        );
    }

    public static TarefaResponse fromPontual(Tarefa t) {
        return from(t, t.isConcluida());
    }
}
