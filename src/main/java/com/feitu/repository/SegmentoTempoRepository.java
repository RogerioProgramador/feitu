package com.feitu.repository;

import com.feitu.domain.SegmentoTempo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SegmentoTempoRepository extends JpaRepository<SegmentoTempo, UUID> {

    List<SegmentoTempo> findByTarefaId(UUID tarefaId);

    Optional<SegmentoTempo> findByTarefaIdAndFimIsNull(UUID tarefaId);

    @Query("SELECT DISTINCT s FROM SegmentoTempo s WHERE s.tarefa.workspace.usuario.id = :usuarioId AND s.inicio >= :de AND s.inicio < :ate")
    List<SegmentoTempo> findByUsuarioIdAndInicioBetween(UUID usuarioId, Instant de, Instant ate);

    void deleteByTarefaId(UUID tarefaId);
}
