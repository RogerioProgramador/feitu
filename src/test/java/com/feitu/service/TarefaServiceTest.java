package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.config.ResourceNotFoundException;
import com.feitu.domain.*;
import com.feitu.dto.TarefaRequest;
import com.feitu.dto.TarefaResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TarefaServiceTest {

    @Mock TarefaRepository tarefaRepository;
    @Mock WorkspaceRepository workspaceRepository;
    @Mock ConclusaoRecorrenteRepository conclusaoRepository;
    @InjectMocks TarefaService service;

    UUID uid = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID tId = UUID.randomUUID();
    Workspace workspace;
    LocalDate hoje = LocalDate.now();

    @BeforeEach
    void setup() {
        workspace = new Workspace();
        workspace.setNome("WS");
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // --- criar ---

    @Test
    void criarPontualSalvaComTipoPontualEDataDeHoje() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));

        TarefaResponse r = service.criar(wsId, uid, new TarefaRequest("Tarefa X", null, "PONTUAL", null, null), hoje);

        assertThat(r.tipo()).isEqualTo(TipoTarefa.PONTUAL);
        assertThat(r.concluida()).isFalse();
    }

    @Test
    void criarRecorrenteSalvaComTipoRecorrenteEDiasSemana() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));

        TarefaResponse r = service.criar(wsId, uid,
                new TarefaRequest("Meditação", null, "RECORRENTE", List.of("SEG", "QUA", "SEX"), null), hoje);

        assertThat(r.tipo()).isEqualTo(TipoTarefa.RECORRENTE);
        assertThat(r.diasSemana()).containsExactlyInAnyOrder("SEG", "QUA", "SEX");
    }

    @Test
    void criarRecorrenteSemDiasLancaBusinessException() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));

        assertThatThrownBy(() -> service.criar(wsId, uid,
                new TarefaRequest("T", null, "RECORRENTE", List.of(), null), hoje))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void criarUsaNomePadraoQuandoVazio() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));

        TarefaResponse r = service.criar(wsId, uid, new TarefaRequest("", null, "PONTUAL", null, null), hoje);

        assertThat(r.nome()).isEqualTo("Nova tarefa");
    }

    // --- concluir / reabrir PONTUAL ---

    @Test
    void concluirPontualSetaConcluidaEConcluidaEm() {
        Tarefa t = tarefaPontual();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        TarefaResponse r = service.concluir(tId, uid, hoje);

        assertThat(r.concluida()).isTrue();
        verify(tarefaRepository).save(t);
        assertThat(t.getConcluidaEm()).isNotNull();
    }

    @Test
    void reabrirPontualLimpaConcluidaEConcluidaEm() {
        Tarefa t = tarefaPontual();
        t.setConcluida(true);
        t.setConcluidaEm(java.time.LocalDateTime.now());
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        TarefaResponse r = service.reabrir(tId, uid, hoje);

        assertThat(r.concluida()).isFalse();
        assertThat(t.getConcluidaEm()).isNull();
    }

    // --- concluir / reabrir RECORRENTE ---

    @Test
    void concluirRecorrenteCriaConclusaoParaData() {
        Tarefa t = tarefaRecorrente();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(conclusaoRepository.findByTarefaIdAndData(tId, hoje)).thenReturn(Optional.empty());

        TarefaResponse r = service.concluir(tId, uid, hoje);

        assertThat(r.concluida()).isTrue();
        verify(conclusaoRepository).save(argThat(c -> c.getData().equals(hoje)));
    }

    @Test
    void concluirRecorrenteNaoDuplicaConclusaoExistente() {
        Tarefa t = tarefaRecorrente();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));
        when(conclusaoRepository.findByTarefaIdAndData(tId, hoje))
                .thenReturn(Optional.of(new ConclusaoRecorrente(t, hoje, java.time.LocalDateTime.now())));

        service.concluir(tId, uid, hoje);

        verify(conclusaoRepository, never()).save(any());
    }

    @Test
    void reabrirRecorrenteDeletaConclusaoDaData() {
        Tarefa t = tarefaRecorrente();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        TarefaResponse r = service.reabrir(tId, uid, hoje);

        assertThat(r.concluida()).isFalse();
        verify(conclusaoRepository).deleteByTarefaIdAndData(tId, hoje);
    }

    // --- listarParaDia ---

    @Test
    void listarParaDiaRetornaPontuaisDoDiaERecorrentesDoDiaSemanaSemMistura() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));
        Tarefa pontual = tarefaPontual();
        Tarefa recorrente = tarefaRecorrente();
        // hoje é terça (TER) neste contexto de teste - usamos data específica
        LocalDate terca = LocalDate.of(2026, 6, 23); // terça-feira
        when(tarefaRepository.findByWorkspaceIdAndData(wsId, terca)).thenReturn(List.of(pontual));
        when(tarefaRepository.findRecorrentesParaDia(wsId, "TER")).thenReturn(List.of(recorrente));
        when(conclusaoRepository.findTarefaIdsConcluidasByIds(Set.of(tId), terca)).thenReturn(Set.of());

        List<TarefaResponse> result = service.listarParaDia(wsId, uid, terca);

        assertThat(result).hasSize(2);
    }

    @Test
    void listarParaDiaExcluiRecorrenteCriadaAposData() {
        when(workspaceRepository.findByIdAndUsuarioId(wsId, uid)).thenReturn(Optional.of(workspace));
        LocalDate terca = LocalDate.of(2026, 6, 23);

        Tarefa futura = tarefaRecorrente();
        // criadoEm amanhã ao meio-dia UTC → dataCriacaoLocalBrasil = amanhã → filtrada
        ReflectionTestUtils.setField(futura, "criadoEm", LocalDateTime.of(2026, 6, 24, 12, 0));

        when(tarefaRepository.findByWorkspaceIdAndData(wsId, terca)).thenReturn(List.of());
        when(tarefaRepository.findRecorrentesParaDia(wsId, "TER")).thenReturn(List.of(futura));

        List<TarefaResponse> result = service.listarParaDia(wsId, uid, terca);

        assertThat(result).isEmpty();
    }

    @Test
    void renomearAtualizaNomeDaTarefa() {
        Tarefa t = tarefaPontual();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        service.renomear(tId, uid, "Novo nome");

        assertThat(t.getNome()).isEqualTo("Novo nome");
        verify(tarefaRepository).save(t);
    }

    @Test
    void atualizarDescricaoSalvaDescricao() {
        Tarefa t = tarefaPontual();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        service.atualizarDescricao(tId, uid, "nova desc");

        assertThat(t.getDescricao()).isEqualTo("nova desc");
        verify(tarefaRepository).save(t);
    }

    @Test
    void deletarRemoveConclussesETarefa() {
        Tarefa t = tarefaPontual();
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.of(t));

        service.deletar(tId, uid);

        verify(conclusaoRepository).deleteByTarefaId(tId);
        verify(tarefaRepository).delete(t);
    }

    @Test
    void tarefaDeOutroUsuarioLancaNotFound() {
        when(tarefaRepository.findByIdAndWorkspaceUsuarioId(tId, uid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.concluir(tId, uid, hoje))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private Tarefa tarefaPontual() {
        Tarefa t = new Tarefa();
        t.setNome("T");
        t.setTipo(TipoTarefa.PONTUAL);
        t.setWorkspace(workspace);
        ReflectionTestUtils.setField(t, "id", tId);
        return t;
    }

    private Tarefa tarefaRecorrente() {
        Tarefa t = new Tarefa();
        t.setNome("Rec");
        t.setTipo(TipoTarefa.RECORRENTE);
        t.setDiasSemana("SEG,QUA,SEX");
        t.setWorkspace(workspace);
        ReflectionTestUtils.setField(t, "id", tId);
        return t;
    }
}
