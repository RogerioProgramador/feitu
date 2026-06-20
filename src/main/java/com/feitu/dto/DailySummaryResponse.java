package com.feitu.dto;

import java.time.LocalDate;
import java.util.List;

public record DailySummaryResponse(
        LocalDate data,
        long totalSegundos,
        List<WorkspaceSummary> porcWorkspace,
        List<TimelineItem> timeline,
        TarefaResumo tarefaMaisLonga
) {}
