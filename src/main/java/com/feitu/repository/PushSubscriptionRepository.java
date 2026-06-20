package com.feitu.repository;

import com.feitu.domain.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {

    List<PushSubscription> findByUsuarioId(UUID usuarioId);

    Optional<PushSubscription> findByUsuarioIdAndEndpoint(UUID usuarioId, String endpoint);

    void deleteByUsuarioIdAndEndpoint(UUID usuarioId, String endpoint);
}
