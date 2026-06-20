package com.feitu.scheduler;

import com.feitu.domain.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class DailyReviewSchedulerTest {

    // Testa apenas a lógica de matching sem Spring context
    private final DailyReviewScheduler scheduler =
            new DailyReviewScheduler(null, null);

    @Test
    void deveEnviarQuandoHorarioCorresponde() {
        Usuario u = usuarioComHorario(LocalTime.of(22, 0));
        assertThat(scheduler.deveEnviar(u, LocalTime.of(22, 0), LocalDate.now())).isTrue();
    }

    @Test
    void naoDeveEnviarQuandoHorarioDiferente() {
        Usuario u = usuarioComHorario(LocalTime.of(22, 0));
        assertThat(scheduler.deveEnviar(u, LocalTime.of(21, 0), LocalDate.now())).isFalse();
    }

    @Test
    void naoDeveEnviarSeJaEnviouHoje() {
        Usuario u = usuarioComHorario(LocalTime.of(22, 0));
        u.setUltimaNotificacaoEm(LocalDate.now());
        assertThat(scheduler.deveEnviar(u, LocalTime.of(22, 0), LocalDate.now())).isFalse();
    }

    @Test
    void deveEnviarSeEnviouOntem() {
        Usuario u = usuarioComHorario(LocalTime.of(22, 0));
        u.setUltimaNotificacaoEm(LocalDate.now().minusDays(1));
        assertThat(scheduler.deveEnviar(u, LocalTime.of(22, 0), LocalDate.now())).isTrue();
    }

    @Test
    void naoDeveEnviarComHorarioNulo() {
        Usuario u = new Usuario();
        u.setHorarioNotificacao(null);
        assertThat(scheduler.deveEnviar(u, LocalTime.of(22, 0), LocalDate.now())).isFalse();
    }

    private Usuario usuarioComHorario(LocalTime horario) {
        Usuario u = new Usuario();
        u.setHorarioNotificacao(horario);
        return u;
    }
}
