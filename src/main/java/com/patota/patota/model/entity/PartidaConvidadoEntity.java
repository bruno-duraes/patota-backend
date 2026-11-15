package com.patota.patota.model.entity;

import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoJogador;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Convidado aguardando aprovação para uma partida, sem cadastro permanente de jogador. */
@Entity
@Table(name = "partida_convidado")
@Getter
@Setter
@NoArgsConstructor
public class PartidaConvidadoEntity implements ParticipanteDaPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    @Column(nullable = false, length = 120)
    private String nome;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int nota = 3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ETipoJogador tipo = ETipoJogador.LINHA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convidado_por_jogador_id", nullable = false)
    private JogadorEntity convidadoPorJogador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EStatusParticipante status = EStatusParticipante.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_partida_id")
    private TimePartidaEntity timePartida;

    @Min(0)
    @Column(nullable = false)
    private int gols = 0;

    @Min(0)
    @Column(nullable = false)
    private int assistencias = 0;

    private LocalDateTime respondidaEm;
}
