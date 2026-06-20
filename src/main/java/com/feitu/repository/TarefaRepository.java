package com.feitu.repository;

import com.feitu.domain.Tarefa;
import com.feitu.domain.TarefaEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {

    List<Tarefa> findByWorkspaceId(UUID workspaceId);

    List<Tarefa> findByWorkspaceIdAndEstado(UUID workspaceId, TarefaEstado estado);

    Optional<Tarefa> findByIdAndWorkspaceUsuarioId(UUID id, UUID usuarioId);
}
