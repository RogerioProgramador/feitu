package com.feitu.repository;

import com.feitu.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository repo;

    @Test
    void salvaEBuscaPorEmail() {
        Usuario u = new Usuario();
        u.setEmail("a@test.com");
        u.setSenhaHash("hash");
        repo.save(u);

        Optional<Usuario> found = repo.findByEmail("a@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("a@test.com");
    }

    @Test
    void emailInexistenteRetornaVazio() {
        assertThat(repo.findByEmail("nao@existe.com")).isEmpty();
    }

    @Test
    void existsByEmail() {
        Usuario u = new Usuario();
        u.setEmail("b@test.com");
        u.setSenhaHash("hash");
        repo.save(u);

        assertThat(repo.existsByEmail("b@test.com")).isTrue();
        assertThat(repo.existsByEmail("outro@test.com")).isFalse();
    }

    @Test
    void emailDuplicadoLancaExcecao() {
        Usuario u1 = new Usuario();
        u1.setEmail("dup@test.com");
        u1.setSenhaHash("hash");
        repo.saveAndFlush(u1);

        Usuario u2 = new Usuario();
        u2.setEmail("dup@test.com");
        u2.setSenhaHash("hash2");

        assertThatThrownBy(() -> repo.saveAndFlush(u2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
