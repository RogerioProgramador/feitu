package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.Tarefa;
import com.feitu.domain.Usuario;
import com.feitu.domain.Workspace;
import com.feitu.dto.WorkspaceRequest;
import com.feitu.repository.ConclusaoRecorrenteRepository;
import com.feitu.repository.TarefaRepository;
import com.feitu.repository.UsuarioRepository;
import com.feitu.repository.WorkspaceRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;
    private final ConclusaoRecorrenteRepository conclusaoRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository, UsuarioRepository usuarioRepository,
                            TarefaRepository tarefaRepository, ConclusaoRecorrenteRepository conclusaoRepository) {
        this.workspaceRepository = workspaceRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.conclusaoRepository = conclusaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Workspace> listar(UUID usuarioId) {
        return workspaceRepository.findByUsuarioIdOrderByOrdem(usuarioId);
    }

    public Workspace criar(UUID usuarioId, WorkspaceRequest req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        int total = workspaceRepository.countByUsuarioId(usuarioId);
        if (total >= 3) throw new BusinessException("Limite de 3 workspaces atingido");

        int proximaOrdem = total + 1;

        Workspace ws = new Workspace();
        ws.setNome(req.nome());
        ws.setCor(req.cor());
        ws.setOrdem(proximaOrdem);
        ws.setUsuario(usuario);
        return workspaceRepository.save(ws);
    }

    public Workspace atualizar(UUID id, UUID usuarioId, WorkspaceRequest req) {
        Workspace ws = buscarDoUsuario(id, usuarioId);
        ws.setNome(req.nome());
        ws.setCor(req.cor());
        return workspaceRepository.save(ws);
    }

    public void reordenar(UUID usuarioId, List<UUID> novaOrdem) {
        List<Workspace> workspaces = workspaceRepository.findByUsuarioIdOrderByOrdem(usuarioId);
        for (int i = 0; i < novaOrdem.size(); i++) {
            UUID id = novaOrdem.get(i);
            workspaces.stream()
                    .filter(ws -> ws.getId().equals(id))
                    .findFirst()
                    .ifPresent(ws -> ws.setOrdem(novaOrdem.indexOf(id) + 1));
        }
        workspaceRepository.saveAll(workspaces);
    }

    public void deletar(UUID id, UUID usuarioId) {
        Workspace ws = buscarDoUsuario(id, usuarioId);
        List<Tarefa> tarefas = tarefaRepository.findByWorkspaceId(id);
        for (Tarefa tarefa : tarefas) {
            conclusaoRepository.deleteByTarefaId(tarefa.getId());
        }
        tarefaRepository.deleteAll(tarefas);
        workspaceRepository.delete(ws);
    }

    private Workspace buscarDoUsuario(UUID id, UUID usuarioId) {
        return workspaceRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace não encontrado"));
    }
}
