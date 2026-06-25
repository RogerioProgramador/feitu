package com.feitu.service;

import com.feitu.domain.PushSubscription;
import com.feitu.domain.Usuario;
import com.feitu.dto.SubscribeRequest;
import com.feitu.repository.PushSubscriptionRepository;
import com.feitu.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PushSubscriptionService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final UsuarioRepository usuarioRepository;

    public PushSubscriptionService(
            PushSubscriptionRepository subscriptionRepository,
            UsuarioRepository usuarioRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void subscribe(UUID usuarioId, SubscribeRequest req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        subscriptionRepository.findByUsuarioIdAndEndpoint(usuarioId, req.endpoint())
                .ifPresentOrElse(
                        existing -> {
                            existing.setP256dh(req.keys().p256dh());
                            existing.setAuth(req.keys().auth());
                            subscriptionRepository.save(existing);
                        },
                        () -> {
                            PushSubscription sub = new PushSubscription();
                            sub.setUsuario(usuario);
                            sub.setEndpoint(req.endpoint());
                            sub.setP256dh(req.keys().p256dh());
                            sub.setAuth(req.keys().auth());
                            subscriptionRepository.save(sub);
                        }
                );
    }

    public void unsubscribe(UUID usuarioId, String endpoint) {
        subscriptionRepository.deleteByUsuarioIdAndEndpoint(usuarioId, endpoint);
    }
}
