package com.feitu.repository;

import com.feitu.domain.ConclusaoRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConclusaoRecorrenteRepository extends JpaRepository<ConclusaoRecorrente, UUID> {

    Optional<ConclusaoRecorrente> findByTarefaIdAndData(UUID tarefaId, LocalDate data);

    void deleteByTarefaIdAndData(UUID tarefaId, LocalDate data);

    void deleteByTarefaId(UUID tarefaId);

    List<ConclusaoRecorrente> findByTarefaIdAndDataBetween(UUID tarefaId, LocalDate from, LocalDate to);
}
