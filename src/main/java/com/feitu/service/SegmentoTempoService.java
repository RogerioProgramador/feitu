package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.domain.SegmentoTempo;
import com.feitu.domain.Tarefa;
import com.feitu.repository.SegmentoTempoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SegmentoTempoService {

    private final SegmentoTempoRepository segmentoRepository;

    public SegmentoTempoService(SegmentoTempoRepository segmentoRepository) {
        this.segmentoRepository = segmentoRepository;
    }

    public SegmentoTempo abrirSegmento(Tarefa tarefa) {
        SegmentoTempo s = new SegmentoTempo();
        s.setTarefa(tarefa);
        s.setInicio(Instant.now());
        return segmentoRepository.save(s);
    }

    public SegmentoTempo fecharSegmentoAberto(Tarefa tarefa) {
        SegmentoTempo s = segmentoRepository.findByTarefaIdAndFimIsNull(tarefa.getId())
                .orElseThrow(() -> new BusinessException("Nenhum segmento aberto para a tarefa"));
        s.setFim(Instant.now());
        return segmentoRepository.save(s);
    }

    @Transactional(readOnly = true)
    public long calcularTempoTotalSegundos(UUID tarefaId) {
        List<SegmentoTempo> segmentos = segmentoRepository.findByTarefaId(tarefaId);
        return segmentos.stream()
                .mapToLong(s -> {
                    Instant fim = s.getFim() != null ? s.getFim() : Instant.now();
                    return Duration.between(s.getInicio(), fim).getSeconds();
                })
                .sum();
    }
}
