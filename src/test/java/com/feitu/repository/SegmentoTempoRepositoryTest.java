package com.feitu.repository;

import com.feitu.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class SegmentoTempoRepositoryTest {

    @Autowired UsuarioRepository usuarioRepo;
    @Autowired WorkspaceRepository workspaceRepo;
    @Autowired TarefaRepository tarefaRepo;
    @Autowired SegmentoTempoRepository segmentoRepo;

    Tarefa tarefa;

    @BeforeEach
    void setup() {
        Usuario u = new Usuario();
        u.setEmail("s@test.com");
        u.setSenhaHash("hash");
        u = usuarioRepo.save(u);

        Workspace ws = new Workspace();
        ws.setNome("WS");
        ws.setOrdem(1);
        ws.setUsuario(u);
        ws = workspaceRepo.save(ws);

        Tarefa t = new Tarefa();
        t.setNome("Tarefa");
        t.setWorkspace(ws);
        tarefa = tarefaRepo.save(t);
    }

    @Test
    void segmentoAbertoRetornadoPorFimIsNull() {
        SegmentoTempo aberto = new SegmentoTempo();
        aberto.setTarefa(tarefa);
        aberto.setInicio(Instant.now());
        segmentoRepo.save(aberto);

        SegmentoTempo fechado = new SegmentoTempo();
        fechado.setTarefa(tarefa);
        fechado.setInicio(Instant.now().minusSeconds(60));
        fechado.setFim(Instant.now());
        segmentoRepo.save(fechado);

        Optional<SegmentoTempo> found = segmentoRepo.findByTarefaIdAndFimIsNull(tarefa.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFim()).isNull();
    }

    @Test
    void semSegmentoAbertoRetornaVazio() {
        SegmentoTempo fechado = new SegmentoTempo();
        fechado.setTarefa(tarefa);
        fechado.setInicio(Instant.now().minusSeconds(30));
        fechado.setFim(Instant.now());
        segmentoRepo.save(fechado);

        assertThat(segmentoRepo.findByTarefaIdAndFimIsNull(tarefa.getId())).isEmpty();
    }

    @Test
    void listaTodosSegmentosDaTarefa() {
        for (int i = 0; i < 3; i++) {
            SegmentoTempo s = new SegmentoTempo();
            s.setTarefa(tarefa);
            s.setInicio(Instant.now().minusSeconds(100 - i));
            s.setFim(Instant.now().minusSeconds(50 - i));
            segmentoRepo.save(s);
        }

        assertThat(segmentoRepo.findByTarefaId(tarefa.getId())).hasSize(3);
    }
}
