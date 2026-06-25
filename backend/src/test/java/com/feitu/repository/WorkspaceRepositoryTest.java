package com.feitu.repository;

import com.feitu.domain.Usuario;
import com.feitu.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class WorkspaceRepositoryTest {

    @Autowired UsuarioRepository usuarioRepo;
    @Autowired WorkspaceRepository workspaceRepo;

    Usuario usuario;

    @BeforeEach
    void setup() {
        Usuario u = new Usuario();
        u.setEmail("w@test.com");
        u.setSenhaHash("hash");
        usuario = usuarioRepo.save(u);
    }

    @Test
    void listaOrdenadoPorOrdem() {
        criarWorkspace("C", 3);
        criarWorkspace("A", 1);
        criarWorkspace("B", 2);

        List<Workspace> lista = workspaceRepo.findByUsuarioIdOrderByOrdem(usuario.getId());

        assertThat(lista).extracting(Workspace::getNome).containsExactly("A", "B", "C");
    }

    @Test
    void buscaPorIdEUsuario() {
        Workspace ws = criarWorkspace("Trabalho", 1);

        Optional<Workspace> found = workspaceRepo.findByIdAndUsuarioId(ws.getId(), usuario.getId());
        assertThat(found).isPresent();
    }

    @Test
    void buscaPorIdEUsuarioErradoRetornaVazio() {
        Workspace ws = criarWorkspace("Trabalho", 1);

        Usuario outro = new Usuario();
        outro.setEmail("outro@test.com");
        outro.setSenhaHash("hash");
        outro = usuarioRepo.save(outro);

        Optional<Workspace> found = workspaceRepo.findByIdAndUsuarioId(ws.getId(), outro.getId());
        assertThat(found).isEmpty();
    }

    private Workspace criarWorkspace(String nome, int ordem) {
        Workspace ws = new Workspace();
        ws.setNome(nome);
        ws.setOrdem(ordem);
        ws.setUsuario(usuario);
        return workspaceRepo.save(ws);
    }
}
