package com.feitu.notification;

import com.feitu.domain.PushSubscription;
import com.feitu.domain.Usuario;
import com.feitu.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final PushService pushService;
    private final boolean enabled;

    public PushNotificationService(
            PushSubscriptionRepository subscriptionRepository,
            @Value("${push.vapid.public-key}") String publicKey,
            @Value("${push.vapid.private-key}") String privateKey,
            @Value("${push.vapid.subject}") String subject) {

        this.subscriptionRepository = subscriptionRepository;

        if (publicKey.isBlank() || privateKey.isBlank()) {
            log.warn("VAPID keys not configured — push notifications disabled");
            this.pushService = null;
            this.enabled = false;
            return;
        }

        Security.addProvider(new BouncyCastleProvider());
        try {
            this.pushService = new PushService(publicKey, privateKey, subject);
            this.enabled = true;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize PushService", e);
        }
    }

    public void enviar(PushSubscription subscription, String titulo, String corpo) {
        if (!enabled) return;

        String payload = "{\"title\":\"" + titulo + "\",\"body\":\"" + corpo + "\"}";
        try {
            nl.martijndwars.webpush.Subscription sub = new nl.martijndwars.webpush.Subscription(
                    subscription.getEndpoint(),
                    new nl.martijndwars.webpush.Subscription.Keys(subscription.getP256dh(), subscription.getAuth())
            );
            Notification notification = new Notification(sub, payload);
            var response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404 || status == 410) {
                log.info("Subscription expirada, removendo: {}", subscription.getEndpoint());
                subscriptionRepository.delete(subscription);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar push para {}: {}", subscription.getEndpoint(), e.getMessage());
        }
    }

    public void enviarParaUsuario(Usuario usuario, String titulo, String corpo) {
        if (!enabled) return;
        List<PushSubscription> subs = subscriptionRepository.findByUsuarioId(usuario.getId());
        subs.forEach(s -> enviar(s, titulo, corpo));
    }
}
