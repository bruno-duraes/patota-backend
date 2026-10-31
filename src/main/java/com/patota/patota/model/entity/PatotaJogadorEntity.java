package com.patota.patota.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Vínculo de um jogador com uma patota. */
@Entity
@Table(
        name = "patota_jogadores",
        uniqueConstraints = @UniqueConstraint(name = "uk_patota_jogador", columnNames = {"patota_id", "jogador_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PatotaJogadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patota_id", nullable = false)
    private PatotaEntity patota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_id", nullable = false)
    private JogadorEntity jogador;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime entrouEm;

    private LocalDateTime saiuEm;

    @PrePersist
    void aoCriar() {
        entrouEm = LocalDateTime.now();
    }
}
