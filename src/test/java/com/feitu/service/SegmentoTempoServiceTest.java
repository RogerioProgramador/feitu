package com.feitu.service;

import com.feitu.domain.*;
import com.feitu.dto.DailySummaryResponse;
import com.feitu.repository.ConclusaoRecorrenteRepository;
import com.feitu.repository.TarefaRepository;
import com.feitu.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes de AnalyticsService (arquivo renomeado de SegmentoTempoServiceTest).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SegmentoTempoServiceTest {

    @Mock TarefaRepository tarefaRepository;
    @Mock WorkspaceRepository workspaceRepository;
    @Mock ConclusaoRecorrenteRepository conclusaoRepository;

    TarefaService tarefaService;
    AnalyticsService analyticsService;

    UUID uid = UUID.randomUUID();
    LocalDate hoje = LocalDate.of(2026, 6, 23); // terça

    @BeforeEach
    void setup() {
        tarefaService = new TarefaService(tarefaRepository, workspaceRepository, conclusaoRepository);
        analyticsService = new AnalyticsService(tarefaService);
    }

    @Test
    void sumarioDiarioComMixRetornaTotalCorreto() {
        Tarefa pontual = pontual(false);
        Tarefa recorrente = recorrente(true);

        when(tarefaRepository.findPontuaisDoUsuarioParaData(uid, hoje)).thenReturn(List.of(pontual));
        when(tarefaRepository.findRecorrentesDoUsuarioParaDia(uid, "TER")).thenReturn(List.of(recorrente));
        when(conclusaoRepository.findTarefaIdsConcluidasByIds(Set.of(recorrente.getId()), hoje))
                .thenReturn(Set.of(recorrente.getId()));

        DailySummaryResponse r = analyticsService.sumarioDiario(uid, hoje);

        assertThat(r.totalTarefas()).isEqualTo(2);
        assertThat(r.concluidas()).isEqualTo(1); // só a recorrente está concluída
        assertThat(r.recorrentes()).hasSize(1);
        assertThat(r.pontuais()).hasSize(1);
    }

    @Test
    void sumarioDiarioTudoConcluido() {
        Tarefa pontual = pontual(true);
        Tarefa recorrente = recorrente(true);

        when(tarefaRepository.findPontuaisDoUsuarioParaData(uid, hoje)).thenReturn(List.of(pontual));
        when(tarefaRepository.findRecorrentesDoUsuarioParaDia(uid, "TER")).thenReturn(List.of(recorrente));
        when(conclusaoRepository.findTarefaIdsConcluidasByIds(Set.of(recorrente.getId()), hoje))
                .thenReturn(Set.of(recorrente.getId()));

        DailySummaryResponse r = analyticsService.sumarioDiario(uid, hoje);

        assertThat(r.concluidas()).isEqualTo(r.totalTarefas());
    }

    @Test
    void sumarioDiarioSemTarefasRetornaZeros() {
        when(tarefaRepository.findPontuaisDoUsuarioParaData(uid, hoje)).thenReturn(List.of());
        when(tarefaRepository.findRecorrentesDoUsuarioParaDia(uid, "TER")).thenReturn(List.of());

        DailySummaryResponse r = analyticsService.sumarioDiario(uid, hoje);

        assertThat(r.totalTarefas()).isZero();
        assertThat(r.concluidas()).isZero();
        assertThat(r.recorrentes()).isEmpty();
        assertThat(r.pontuais()).isEmpty();
    }

    private Tarefa pontual(boolean concluida) {
        Tarefa t = new Tarefa();
        t.setNome("Pontual");
        t.setTipo(TipoTarefa.PONTUAL);
        t.setConcluida(concluida);
        return t;
    }

    private Tarefa recorrente(boolean concluida) {
        Tarefa t = new Tarefa();
        t.setNome("Rec");
        t.setTipo(TipoTarefa.RECORRENTE);
        t.setDiasSemana("TER,SEX");
        try {
            var f = Tarefa.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(t, UUID.randomUUID());
        } catch (Exception ignored) {}
        return t;
    }
}
