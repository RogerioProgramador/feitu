package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.*;
import com.feitu.dto.TarefaRequest;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TarefaServiceTest {

    @Mock TarefaRepository tarefaRepository;
    @Mock WorkspaceRepository workspaceRepository;
    @Mock SegmentoTempoService segmentoTempoService;
    @InjectMocks TarefaService service;

    UUID uid = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID tId = UUID.randomUUID();
    Workspace workspace;

    @BeforeEach
    void setup() {
        workspace = new Workspace();
        workspace.setNome("WS");
    }

    @Test
    void criarUsaNomeDefaultQuandoNulo() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Tarefa t = service.criar(wsId, uid, new TarefaRequest(null));
        assertThat(t.getNome()).isEqualTo("Outros");
    }

    @Test
    void iniciarDeIDLEMudaParaRUNNING() {
        Tarefa t = tarefaComEstado(TarefaEstado.IDLE);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Tarefa result = service.iniciarTimer(tId, uid);
        assertThat(result.getEstado()).isEqualTo(TarefaEstado.RUNNING);
        verify(segmentoTempoService).abrirSegmento(t);
    }

    @Test
    void iniciarDePAUSEDMudaParaRUNNING() {
        Tarefa t = tarefaComEstado(TarefaEstado.PAUSED);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.iniciarTimer(tId, uid);
        assertThat(t.getEstado()).isEqualTo(TarefaEstado.RUNNING);
    }

    @Test
    void iniciarDeRUNNINGLancaExcecao() {
        Tarefa t = tarefaComEstado(TarefaEstado.RUNNING);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.iniciarTimer(tId, uid)).isInstanceOf(BusinessException.class);
    }

    @Test
    void pausarDeRUNNINGMudaParaPAUSED() {
        Tarefa t = tarefaComEstado(TarefaEstado.RUNNING);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.pausarTimer(tId, uid);
        assertThat(t.getEstado()).isEqualTo(TarefaEstado.PAUSED);
        verify(segmentoTempoService).fecharSegmentoAberto(t);
    }

    @Test
    void pararDeRUNNINGMudaParaDONE() {
        Tarefa t = tarefaComEstado(TarefaEstado.RUNNING);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.pararTimer(tId, uid);
        assertThat(t.getEstado()).isEqualTo(TarefaEstado.DONE);
        assertThat(t.getConcluidoEm()).isNotNull();
    }

    @Test
    void pararDePAUSEDNaoFechaSegmento() {
        Tarefa t = tarefaComEstado(TarefaEstado.PAUSED);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.pararTimer(tId, uid);
        assertThat(t.getEstado()).isEqualTo(TarefaEstado.DONE);
        verify(segmentoTempoService, never()).fecharSegmentoAberto(any());
    }

    @Test
    void reativarDeDONEVoltaParaIDLE() {
        Tarefa t = tarefaComEstado(TarefaEstado.DONE);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.reativar(tId, uid);
        assertThat(t.getEstado()).isEqualTo(TarefaEstado.IDLE);
        assertThat(t.getConcluidoEm()).isNull();
    }

    @Test
    void reativarDeNaoDONELancaExcecao() {
        Tarefa t = tarefaComEstado(TarefaEstado.RUNNING);
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.reativar(tId, uid)).isInstanceOf(BusinessException.class);
    }

    @Test
    void tarefaDeOutroUsuarioLancaNotFound() {
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarTimer(tId, uid)).isInstanceOf(ResourceNotFoundException.class);
    }

    private Tarefa tarefaComEstado(TarefaEstado estado) {
        Tarefa t = new Tarefa();
        t.setNome("T");
        t.setEstado(estado);
        t.setWorkspace(workspace);
        return t;
    }
}
