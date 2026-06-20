package com.feitu.service;

import com.feitu.domain.SegmentoTempo;
import com.feitu.dto.*;
import com.feitu.repository.SegmentoTempoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SegmentoTempoRepository segmentoRepository;

    public AnalyticsService(SegmentoTempoRepository segmentoRepository) {
        this.segmentoRepository = segmentoRepository;
    }

    public DailySummaryResponse sumarioDiario(UUID usuarioId, LocalDate data) {
        ZoneId zone = ZoneId.systemDefault();
        Instant de = data.atStartOfDay(zone).toInstant();
        Instant ate = data.plusDays(1).atStartOfDay(zone).toInstant();

        List<SegmentoTempo> segmentos = segmentoRepository
                .findByUsuarioIdAndInicioBetween(usuarioId, de, ate)
                .stream()
                .filter(s -> s.getFim() != null)
                .toList();

        long totalSegundos = segmentos.stream()
                .mapToLong(s -> Duration.between(s.getInicio(), s.getFim()).getSeconds())
                .sum();

        // Tempo por workspace
        Map<UUID, Long> tempoWs = new LinkedHashMap<>();
        Map<UUID, String> nomeWs = new HashMap<>();
        Map<UUID, String> corWs = new HashMap<>();

        for (SegmentoTempo s : segmentos) {
            var ws = s.getTarefa().getWorkspace();
            long dur = Duration.between(s.getInicio(), s.getFim()).getSeconds();
            tempoWs.merge(ws.getId(), dur, Long::sum);
            nomeWs.put(ws.getId(), ws.getNome());
            corWs.put(ws.getId(), ws.getCor());
        }

        List<WorkspaceSummary> porcWorkspace = tempoWs.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(e -> new WorkspaceSummary(e.getKey(), nomeWs.get(e.getKey()), corWs.get(e.getKey()), e.getValue()))
                .toList();

        // Timeline cronológica
        List<TimelineItem> timeline = segmentos.stream()
                .sorted(Comparator.comparing(SegmentoTempo::getInicio))
                .map(s -> new TimelineItem(
                        s.getTarefa().getId(),
                        s.getTarefa().getNome(),
                        s.getTarefa().getWorkspace().getCor(),
                        s.getInicio(),
                        s.getFim()))
                .toList();

        // Tarefa com mais tempo
        Map<UUID, Long> tempoPorTarefa = segmentos.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTarefa().getId(),
                        Collectors.summingLong(s -> Duration.between(s.getInicio(), s.getFim()).getSeconds())
                ));

        TarefaResumo tarefaMaisLonga = tempoPorTarefa.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .flatMap(e -> segmentos.stream()
                        .filter(s -> s.getTarefa().getId().equals(e.getKey()))
                        .findFirst()
                        .map(s -> new TarefaResumo(e.getKey(), s.getTarefa().getNome(), e.getValue())))
                .orElse(null);

        return new DailySummaryResponse(data, totalSegundos, porcWorkspace, timeline, tarefaMaisLonga);
    }
}
