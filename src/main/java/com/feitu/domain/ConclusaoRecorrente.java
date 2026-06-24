package com.feitu.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "conclusoes_recorrentes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tarefa_id", "data"})
)
public class ConclusaoRecorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalDateTime concluidaEm;

    public ConclusaoRecorrente() {}

    public ConclusaoRecorrente(Tarefa tarefa, LocalDate data, LocalDateTime concluidaEm) {
        this.tarefa = tarefa;
        this.data = data;
        this.concluidaEm = concluidaEm;
    }

    public UUID getId() { return id; }
    public Tarefa getTarefa() { return tarefa; }
    public LocalDate getData() { return data; }
    public LocalDateTime getConcluidaEm() { return concluidaEm; }
}
