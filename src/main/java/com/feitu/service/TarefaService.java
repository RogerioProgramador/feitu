package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.Tarefa;
import com.feitu.domain.TarefaEstado;
import com.feitu.domain.Workspace;
import com.feitu.dto.TarefaRequest;
import com.feitu.repository.TarefaRepository;
import com.feitu.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SegmentoTempoService segmentoTempoService;

    public TarefaService(
            TarefaRepository tarefaRepository,
            WorkspaceRepository workspaceRepository,
            SegmentoTempoService segmentoTempoService) {
        this.tarefaRepository = tarefaRepository;
        this.workspaceRepository = workspaceRepository;
        this.segmentoTempoService = segmentoTempoService;
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listar(UUID workspaceId, UUID usuarioId) {
        buscarWorkspaceDoUsuario(workspaceId, usuarioId);
        return tarefaRepository.findByWorkspaceId(workspaceId);
    }

    public Tarefa criar(UUID workspaceId, UUID usuarioId, TarefaRequest req) {
        Workspace ws = buscarWorkspaceDoUsuario(workspaceId, usuarioId);
        Tarefa t = new Tarefa();
        t.setNome(req.nome() == null || req.nome().isBlank() ? "Outros" : req.nome());
        t.setWorkspace(ws);
        return tarefaRepository.save(t);
    }

    public Tarefa renomear(UUID id, UUID usuarioId, String nome) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        t.setNome(nome);
        return tarefaRepository.save(t);
    }

    public Tarefa iniciarTimer(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        if (t.getEstado() != TarefaEstado.IDLE && t.getEstado() != TarefaEstado.PAUSED) {
            throw new BusinessException("Não é possível iniciar uma tarefa com estado " + t.getEstado());
        }
        t.setEstado(TarefaEstado.RUNNING);
        segmentoTempoService.abrirSegmento(t);
        return tarefaRepository.save(t);
    }

    public Tarefa pausarTimer(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        if (t.getEstado() != TarefaEstado.RUNNING) {
            throw new BusinessException("Não é possível pausar uma tarefa com estado " + t.getEstado());
        }
        segmentoTempoService.fecharSegmentoAberto(t);
        t.setEstado(TarefaEstado.PAUSED);
        return tarefaRepository.save(t);
    }

    public Tarefa pararTimer(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        if (t.getEstado() != TarefaEstado.RUNNING && t.getEstado() != TarefaEstado.PAUSED) {
            throw new BusinessException("Não é possível parar uma tarefa com estado " + t.getEstado());
        }
        if (t.getEstado() == TarefaEstado.RUNNING) {
            segmentoTempoService.fecharSegmentoAberto(t);
        }
        t.setEstado(TarefaEstado.DONE);
        t.setConcluidoEm(LocalDateTime.now());
        return tarefaRepository.save(t);
    }

    public Tarefa reativar(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        if (t.getEstado() != TarefaEstado.DONE) {
            throw new BusinessException("Somente tarefas DONE podem ser reativadas");
        }
        t.setEstado(TarefaEstado.IDLE);
        t.setConcluidoEm(null);
        return tarefaRepository.save(t);
    }

    public void deletar(UUID id, UUID usuarioId) {
        Tarefa t = buscarDoUsuario(id, usuarioId);
        segmentoTempoService.deletarPorTarefa(id);
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
}
