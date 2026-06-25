package com.feitu.repository;

import com.feitu.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByUsuarioIdOrderByOrdem(UUID usuarioId);

    Optional<Workspace> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    int countByUsuarioId(UUID usuarioId);
}
