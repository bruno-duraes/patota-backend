package com.patota.patota.model.entity;

import com.patota.patota.model.enums.EStatusParticipante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mensalista convocado para uma partida, inicialmente pendente. */
@Entity
@Table(
        name = "jogador_partida",
        uniqueConstraints = @UniqueConstraint(name = "uk_partida_jogador", columnNames = {"partida_id", "jogador_id"}))
@Getter
@Setter
@NoArgsConstructor
public class JogadorPartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_id", nullable = false)
    private JogadorEntity jogador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EStatusParticipante status = EStatusParticipante.PENDENTE;

    @OneToOne(mappedBy = "jogadorPartida", fetch = FetchType.LAZY)
    private EscalacaoPartidaEntity escalacao;

    private LocalDateTime respondidaEm;
}
