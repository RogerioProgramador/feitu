package com.feitu.dto;

import java.time.LocalDate;
import java.util.List;

public record DailySummaryResponse(
        LocalDate data,
        int totalTarefas,
        int concluidas,
        List<TarefaItemResponse> recorrentes,
        List<TarefaItemResponse> pontuais
) {}
