package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.ConclusaoRecorrente;
import com.feitu.domain.Tarefa;
import com.feitu.domain.TipoTarefa;
import com.feitu.domain.Workspace;
import com.feitu.dto.TarefaRequest;
import com.feitu.dto.TarefaResponse;
import com.feitu.repository.ConclusaoRecorrenteRepository;
import com.feitu.repository.TarefaRepository;
import com.feitu.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TarefaService {

    private static final Map<DayOfWeek, String> DIA_PT = Map.of(
            DayOfWeek.MONDAY, "SEG",
            DayOfWeek.TUESDAY, "TER",
            DayOfWeek.WEDNESDAY, "QUA",
            DayOfWeek.THURSDAY, "QUI",
            DayOfWeek.FRIDAY, "SEX",
            DayOfWeek.SATURDAY, "SAB",
            DayOfWeek.SUNDAY, "DOM"
    );

    private final TarefaRepository tarefaRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ConclusaoRecorrenteRepository conclusaoRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            WorkspaceRepository workspaceRepository,
            ConclusaoRecorrenteRepository conclusaoRepository) {
        this.tarefaRepository = tarefaRepository;
        this.workspaceRepository = workspaceRepository;
        this.conclusaoRepository = conclusaoRepository;
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listarParaDia(UUID workspaceId, UUID usuarioId, LocalDate date) {
        buscarWorkspaceDoUsuario(workspaceId, usuarioId);

        List<Tarefa> pontuais = tarefaRepository.findByWorkspaceIdAndData(workspaceId, date);

        String diaSemana = DIA_PT.get(date.getDayOfWeek());
        List<Tarefa> recorrentes = tarefaRepository.findRecorrentesParaDia(workspaceId, diaSemana);

        // Buscar conclusões das recorrentes em batch para evitar N+1
        Set<UUID> idsRecorrentes = recorrentes.stream().map(Tarefa::getId).collect(Collectors.toSet());
        Set<UUID> concluidasHoje = idsRecorrentes.isEmpty() ? Set.of() :
                recorrentes.stream()
                        .filter(t -> conclusaoRepository.findByTarefaIdAndData(t.getId(), date).isPresent())
                        .map(Tarefa::getId)
                        .collect(Collectors.toSet());

        List<TarefaResponse> result = new java.util.ArrayList<>();
        recorrentes.stream()
                .map(t -> TarefaResponse.from(t, concluidasHoje.contains(t.getId())))
                .forEach(result::add);
        pontuais.stream()
                .map(TarefaResponse::fromPontual)
                .forEach(result::add);

        return result;
    }

    public TarefaResponse criar(UUID workspaceId, UUID usuarioId, TarefaRequest req, LocalDate date) {
        Workspace ws = buscarWorkspaceDoUsuario(workspaceId, usuarioId);

        TipoTarefa tipo = "RECORRENTE".equalsIgnoreCase(req.tipo()) ? TipoTarefa.RECORRENTE : TipoTarefa.PONTUAL;

        if (tipo == TipoTarefa.RECORRENTE) {
            if (req.diasSemana() == null || req.diasSemana().isEmpty()) {
                throw new BusinessException("Tarefa recorrente requer ao menos um dia da semana");
            }
        }

        Tarefa t = new Tarefa();
        t.setNome(req.nome() == null || req.nome().isBlank() ? "Nova tarefa" : req.nome());
        t.setDescricao(req.descricao());
        t.setTipo(tipo);
        t.setWorkspace(ws);

        if (tipo == TipoTarefa.RECORRENTE) {
            t.setDiasSemana(String.join(",", req.diasSemana()));
            if (req.horario() != null && !req.horario().isBlank()) {
                t.setHorario(LocalTime.parse(req.horario()));
            }
        } else {
            t.setData(date);
        }

        return TarefaResponse.fromPontual(tarefaRepository.save(t));
    }

    public TarefaResponse renomear(UUID id, UUID usuarioId, String nome) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        t.setNome(nome);
        return TarefaResponse.fromPontual(tarefaRepository.save(t));
    }

    public TarefaResponse atualizarDescricao(UUID id, UUID usuarioId, String descricao) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        t.setDescricao(descricao);
        return TarefaResponse.fromPontual(tarefaRepository.save(t));
    }

    public TarefaResponse concluir(UUID id, UUID usuarioId, LocalDate date) {
        Tarefa t = buscarDoUsuario(id, usuarioId);

        if (t.getTipo() == TipoTarefa.RECORRENTE) {
            if (conclusaoRepository.findByTarefaIdAndData(id, date).isEmpty()) {
                conclusaoRepository.save(new ConclusaoRecorrente(t, date, LocalDateTime.now()));
            }
            return TarefaResponse.from(t, true);
        }

        t.setConcluida(true);
        t.setConcluidaEm(LocalDateTime.now());
        return TarefaResponse.fromPontual(tarefaRepository.save(t));
    }

    public TarefaResponse reabrir(UUID id, UUID usuarioId, LocalDate date) {
        Tarefa t = buscarDoUsuario(id, usuarioId);

        if (t.getTipo() == TipoTarefa.RECORRENTE) {
            conclusaoRepository.deleteByTarefaIdAndData(id, date);
            return TarefaResponse.from(t, false);
        }

        t.setConcluida(false);
        t.setConcluidaEm(null);
        return TarefaResponse.fromPontual(tarefaRepository.save(t));
    }

    public void deletar(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        conclusaoRepository.deleteByTarefaId(id);
        tarefaRepository.delete(t);
    }

    private Tarefa buscarDoUsuario(UUID id, UUID usuarioId) {
        return tarefaRepository.findByIdAndWorkspaceUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
    }

    private Workspace buscarWorkspaceDoUsuario(UUID workspaceId, UUID usuarioId) {
        return workspaceRepository.findByIdAndUsuarioId(workspaceId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace não encontrado"));
    }

    /** Utilitário usado por AnalyticsService */
    public List<TarefaResponse> listarParaDiaDoUsuario(UUID usuarioId, LocalDate date) {
        String diaSemana = DIA_PT.get(date.getDayOfWeek());

        List<Tarefa> pontuais = tarefaRepository.findPontuaisDoUsuarioParaData(usuarioId, date);
        List<Tarefa> recorrentes = tarefaRepository.findRecorrentesDoUsuarioParaDia(usuarioId, diaSemana);

        Set<UUID> concluidasHoje = recorrentes.stream()
                .filter(t -> conclusaoRepository.findByTarefaIdAndData(t.getId(), date).isPresent())
                .map(Tarefa::getId)
                .collect(Collectors.toSet());

        List<TarefaResponse> result = new java.util.ArrayList<>();
        recorrentes.stream()
                .map(t -> TarefaResponse.from(t, concluidasHoje.contains(t.getId())))
                .forEach(result::add);
        pontuais.stream()
                .map(TarefaResponse::fromPontual)
                .forEach(result::add);

        return result;
    }
}
