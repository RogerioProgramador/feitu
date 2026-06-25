package com.feitu.service;

import com.feitu.domain.TipoTarefa;
import com.feitu.dto.DailySummaryResponse;
import com.feitu.dto.TarefaItemResponse;
import com.feitu.dto.TarefaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TarefaService tarefaService;

    public AnalyticsService(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    public DailySummaryResponse sumarioDiario(UUID usuarioId, LocalDate data) {
        List<TarefaResponse> todas = tarefaService.listarParaDiaDoUsuario(usuarioId, data);

        List<TarefaItemResponse> recorrentes = todas.stream()
                .filter(t -> t.tipo() == TipoTarefa.RECORRENTE)
                .map(t -> new TarefaItemResponse(t.id(), t.nome(), t.concluida(), t.tipo()))
                .toList();

        List<TarefaItemResponse> pontuais = todas.stream()
                .filter(t -> t.tipo() == TipoTarefa.PONTUAL)
                .map(t -> new TarefaItemResponse(t.id(), t.nome(), t.concluida(), t.tipo()))
                .toList();

        int total = todas.size();
        int concluidas = (int) todas.stream().filter(TarefaResponse::concluida).count();

        return new DailySummaryResponse(data, total, concluidas, recorrentes, pontuais);
    }
}
