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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkspaceServiceTest {

    @Mock WorkspaceRepository workspaceRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TarefaRepository tarefaRepository;
    @Mock ConclusaoRecorrenteRepository conclusaoRepository;
    @InjectMocks WorkspaceService service;

    UUID usuarioId = UUID.randomUUID();
    Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setEmail("u@test.com");
    }

    @Test
    void criarAtribuiOrdemCorreta() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(workspaceRepository.countByUsuarioId(usuarioId)).thenReturn(2);
        when(workspaceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Workspace ws = service.criar(usuarioId, new WorkspaceRequest("Trabalho", "#A7C7E7"));

        assertThat(ws.getOrdem()).isEqualTo(3);
        assertThat(ws.getNome()).isEqualTo("Trabalho");
    }

    @Test
    void atualizarWorkspaceDeOutroUsuarioLancaNotFound() {
        when(workspaceRepository.findByIdAndUsuarioId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(UUID.randomUUID(), usuarioId, new WorkspaceRequest("X", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletarWorkspaceDeOutroUsuarioLancaNotFound() {
        when(workspaceRepository.findByIdAndUsuarioId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(UUID.randomUUID(), usuarioId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletarWorkspaceDeletaConclussesETarefasEmCascata() {
        UUID wsId = UUID.randomUUID();
        Workspace ws = new Workspace();
        ws.setNome("WS");

        Tarefa tarefa = new Tarefa();
        UUID tarefaId = UUID.randomUUID();

        when(workspaceRepository.findByIdAndUsuarioId(wsId, usuarioId)).thenReturn(Optional.of(ws));
        when(tarefaRepository.findByWorkspaceId(wsId)).thenReturn(List.of(tarefa));

        service.deletar(wsId, usuarioId);

        verify(conclusaoRepository).deleteByTarefaId(any());
        verify(tarefaRepository).deleteAll(List.of(tarefa));
        verify(workspaceRepository).delete(ws);
    }

    @Test
    void criarComTresWorkspacesLancaBusinessException() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(workspaceRepository.countByUsuarioId(usuarioId)).thenReturn(3);

        assertThatThrownBy(() -> service.criar(usuarioId, new WorkspaceRequest("4º WS", "#A7C7E7")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Limite");
    }

    @Test
    void listarRetornaListaDoRepositorio() {
        Workspace ws = new Workspace();
        ws.setNome("W");
        when(workspaceRepository.findByUsuarioIdOrderByOrdem(usuarioId)).thenReturn(List.of(ws));

        List<Workspace> result = service.listar(usuarioId);

        assertThat(result).hasSize(1);
    }
}
