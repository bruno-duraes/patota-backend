package com.patota.patota.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Escalação de um mensalista em um time da partida. Convidados serão tratados depois. */
@Entity
@Table(name = "escalacoes_partida")
@Getter
@Setter
@NoArgsConstructor
public class EscalacaoPartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_partida_id", nullable = false)
    private TimePartidaEntity timePartida;

    /**
     * Um jogador da partida só pode integrar uma escalação. A regra de negócio
     * exige confirmação e vínculo com a mesma partida do time.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_partida_id", nullable = false, unique = true)
    private JogadorPartidaEntity jogadorPartida;

    @Min(0)
    @Column(nullable = false)
    private int gols = 0;

    @Min(0)
    @Column(nullable = false)
    private int assistencias = 0;
}
