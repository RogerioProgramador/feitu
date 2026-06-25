package com.feitu.repository;

import com.feitu.domain.ConclusaoRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ConclusaoRecorrenteRepository extends JpaRepository<ConclusaoRecorrente, UUID> {

    Optional<ConclusaoRecorrente> findByTarefaIdAndData(UUID tarefaId, LocalDate data);

    void deleteByTarefaIdAndData(UUID tarefaId, LocalDate data);

    void deleteByTarefaId(UUID tarefaId);

    List<ConclusaoRecorrente> findByTarefaIdAndDataBetween(UUID tarefaId, LocalDate from, LocalDate to);

    @Query("SELECT c.tarefa.id FROM ConclusaoRecorrente c WHERE c.tarefa.id IN :ids AND c.data = :data")
    Set<UUID> findTarefaIdsConcluidasByIds(@Param("ids") Set<UUID> ids, @Param("data") LocalDate data);

    @Modifying
    @Query("DELETE FROM ConclusaoRecorrente c WHERE c.tarefa.id IN (SELECT t.id FROM Tarefa t WHERE t.workspace.id = :workspaceId)")
    void deleteByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
