package com.feitu.repository;

import com.feitu.domain.Tarefa;
import com.feitu.domain.TipoTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {

    List<Tarefa> findByWorkspaceId(UUID workspaceId);

    List<Tarefa> findByWorkspaceIdAndTipo(UUID workspaceId, TipoTarefa tipo);

    List<Tarefa> findByWorkspaceIdAndData(UUID workspaceId, LocalDate data);

    Optional<Tarefa> findByIdAndWorkspaceUsuarioId(UUID id, UUID usuarioId);

    /** Recorrentes de um workspace que incluem o dia da semana e foram criadas até a data consultada. */
    @Query("SELECT t FROM Tarefa t WHERE t.workspace.id = :workspaceId AND t.tipo = 'RECORRENTE' AND t.diasSemana LIKE CONCAT('%', :diaSemana, '%') AND CAST(t.criadoEm AS date) <= :data")
    List<Tarefa> findRecorrentesParaDia(@Param("workspaceId") UUID workspaceId, @Param("diaSemana") String diaSemana, @Param("data") LocalDate data);

    /** Pontuais de um usuário para uma data específica (todos os workspaces). */
    @Query("SELECT t FROM Tarefa t WHERE t.workspace.usuario.id = :usuarioId AND t.tipo = 'PONTUAL' AND t.data = :data")
    List<Tarefa> findPontuaisDoUsuarioParaData(@Param("usuarioId") UUID usuarioId, @Param("data") LocalDate data);

    /** Recorrentes de um usuário para um dia da semana específico (todos os workspaces), criadas até a data consultada. */
    @Query("SELECT t FROM Tarefa t WHERE t.workspace.usuario.id = :usuarioId AND t.tipo = 'RECORRENTE' AND t.diasSemana LIKE CONCAT('%', :diaSemana, '%') AND CAST(t.criadoEm AS date) <= :data")
    List<Tarefa> findRecorrentesDoUsuarioParaDia(@Param("usuarioId") UUID usuarioId, @Param("diaSemana") String diaSemana, @Param("data") LocalDate data);
}
