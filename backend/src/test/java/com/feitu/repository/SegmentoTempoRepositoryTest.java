package com.feitu.repository;

import com.feitu.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de ConclusaoRecorrenteRepository (arquivo mantido como SegmentoTempoRepositoryTest).
 */
@DataJpaTest
class SegmentoTempoRepositoryTest {

    @Autowired UsuarioRepository usuarioRepo;
    @Autowired WorkspaceRepository workspaceRepo;
    @Autowired TarefaRepository tarefaRepo;
    @Autowired ConclusaoRecorrenteRepository conclusaoRepo;

    Tarefa tarefa;

    @BeforeEach
    void setup() {
        Usuario u = new Usuario();
        u.setEmail("c@test.com");
        u.setSenhaHash("hash");
        u = usuarioRepo.save(u);

        Workspace ws = new Workspace();
        ws.setNome("WS");
        ws.setOrdem(1);
        ws.setUsuario(u);
        ws = workspaceRepo.save(ws);

        Tarefa t = new Tarefa();
        t.setNome("Meditação");
        t.setTipo(TipoTarefa.RECORRENTE);
        t.setDiasSemana("SEG,QUA,SEX");
        t.setWorkspace(ws);
        tarefa = tarefaRepo.save(t);
    }

    @Test
    void buscaPorTarefaIdEData() {
        LocalDate hoje = LocalDate.now();
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, hoje, LocalDateTime.now()));

        Optional<ConclusaoRecorrente> found = conclusaoRepo.findByTarefaIdAndData(tarefa.getId(), hoje);
        assertThat(found).isPresent();
        assertThat(found.get().getData()).isEqualTo(hoje);
    }

    @Test
    void datasDiferentesNaoColidem() {
        LocalDate d1 = LocalDate.of(2026, 6, 22);
        LocalDate d2 = LocalDate.of(2026, 6, 23);
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, d1, LocalDateTime.now()));
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, d2, LocalDateTime.now()));

        assertThat(conclusaoRepo.findByTarefaIdAndData(tarefa.getId(), d1)).isPresent();
        assertThat(conclusaoRepo.findByTarefaIdAndData(tarefa.getId(), d2)).isPresent();
    }

    @Test
    void deleteByTarefaIdEDataRemoveSomenteAlvo() {
        LocalDate d1 = LocalDate.of(2026, 6, 22);
        LocalDate d2 = LocalDate.of(2026, 6, 23);
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, d1, LocalDateTime.now()));
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, d2, LocalDateTime.now()));

        conclusaoRepo.deleteByTarefaIdAndData(tarefa.getId(), d1);
        conclusaoRepo.flush();

        assertThat(conclusaoRepo.findByTarefaIdAndData(tarefa.getId(), d1)).isEmpty();
        assertThat(conclusaoRepo.findByTarefaIdAndData(tarefa.getId(), d2)).isPresent();
    }

    @Test
    void duplicataTarefaDataLancaViolacao() {
        LocalDate hoje = LocalDate.now();
        conclusaoRepo.save(new ConclusaoRecorrente(tarefa, hoje, LocalDateTime.now()));

        assertThatThrownBy(() -> {
            conclusaoRepo.save(new ConclusaoRecorrente(tarefa, hoje, LocalDateTime.now()));
            conclusaoRepo.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
