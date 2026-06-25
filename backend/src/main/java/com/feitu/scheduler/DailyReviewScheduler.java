package com.feitu.scheduler;

import com.feitu.domain.Usuario;
import com.feitu.notification.PushNotificationService;
import com.feitu.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DailyReviewScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyReviewScheduler.class);

    private final UsuarioRepository usuarioRepository;
    private final PushNotificationService pushService;

    public DailyReviewScheduler(UsuarioRepository usuarioRepository, PushNotificationService pushService) {
        this.usuarioRepository = usuarioRepository;
        this.pushService = pushService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void enviarRevisaoDiaria() {
        LocalTime agora = LocalTime.now().withSecond(0).withNano(0);
        LocalDate hoje = LocalDate.now();

        List<Usuario> usuarios = usuarioRepository.findByHorarioNotificacao(agora);
        for (Usuario usuario : usuarios) {
            if (!deveEnviar(usuario, agora, hoje)) continue;

            pushService.enviarParaUsuario(
                    usuario,
                    "Feitu — Revisão Diária",
                    "Como foi seu dia? Veja o resumo das suas tarefas."
            );

            usuario.setUltimaNotificacaoEm(hoje);
            usuarioRepository.save(usuario);
            log.info("Notificação enviada para {}", usuario.getEmail());
        }
    }

    boolean deveEnviar(Usuario usuario, LocalTime agora, LocalDate hoje) {
        LocalTime horario = usuario.getHorarioNotificacao();
        if (horario == null) return false;
        if (!horario.equals(agora)) return false;
        return !hoje.equals(usuario.getUltimaNotificacaoEm());
    }
}
