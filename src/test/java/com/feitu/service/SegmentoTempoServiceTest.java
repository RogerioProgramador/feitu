package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.domain.SegmentoTempo;
import com.feitu.domain.Tarefa;
import com.feitu.repository.SegmentoTempoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SegmentoTempoServiceTest {

    @Mock SegmentoTempoRepository repo;
    @InjectMocks SegmentoTempoService service;

    Tarefa tarefa = new Tarefa();
    UUID tarefaId = UUID.randomUUID();

    @Test
    void abrirSegmentoCriaComInicioEFimNulo() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        SegmentoTempo s = service.abrirSegmento(tarefa);

        assertThat(s.getInicio()).isNotNull();
        assertThat(s.getFim()).isNull();
    }

    @Test
    void fecharSegmentoSetaFim() {
        SegmentoTempo aberto = new SegmentoTempo();
        aberto.setInicio(Instant.now().minusSeconds(30));
        when(repo.findByTarefaIdAndFimIsNull(any())).thenReturn(Optional.of(aberto));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        SegmentoTempo fechado = service.fecharSegmentoAberto(tarefa);

        assertThat(fechado.getFim()).isNotNull();
    }

    @Test
    void fecharSemAbertosLancaExcecao() {
        when(repo.findByTarefaIdAndFimIsNull(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fecharSegmentoAberto(tarefa))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void calcularTempoIgnoraSegmentosAbertos() {
        SegmentoTempo fechado = new SegmentoTempo();
        fechado.setInicio(Instant.now().minusSeconds(60));
        fechado.setFim(Instant.now().minusSeconds(0));

        SegmentoTempo aberto = new SegmentoTempo();
        aberto.setInicio(Instant.now().minusSeconds(10));

        when(repo.findByTarefaId(tarefaId)).thenReturn(List.of(fechado, aberto));

        long total = service.calcularTempoTotalSegundos(tarefaId);

        assertThat(total).isGreaterThan(0).isLessThanOrEqualTo(60);
    }
}
