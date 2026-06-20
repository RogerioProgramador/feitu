package com.feitu.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalTime horarioNotificacao = LocalTime.of(22, 0);

    private LocalDate ultimaNotificacaoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalTime getHorarioNotificacao() { return horarioNotificacao; }
    public void setHorarioNotificacao(LocalTime horarioNotificacao) { this.horarioNotificacao = horarioNotificacao; }
    public LocalDate getUltimaNotificacaoEm() { return ultimaNotificacaoEm; }
    public void setUltimaNotificacaoEm(LocalDate ultimaNotificacaoEm) { this.ultimaNotificacaoEm = ultimaNotificacaoEm; }
}
