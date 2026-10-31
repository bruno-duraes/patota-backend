package com.patota.patota.model.entity;

import com.patota.patota.model.enums.EStatusPresenca;
import com.patota.patota.model.enums.ETipoParticipacaoPartida;
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

@Entity
@Table(
        name = "presencas_partida",
        uniqueConstraints = @UniqueConstraint(name = "uk_partida_jogador", columnNames = {"partida_id", "jogador_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PresencaPartidaEntity {

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
    private EStatusPresenca status = EStatusPresenca.PENDENTE;

    /**
     * Mensalistas precisam ter um vínculo ativo com a patota da partida.
     * Convidados participam somente desta partida e não geram esse vínculo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ETipoParticipacaoPartida tipoParticipacao = ETipoParticipacaoPartida.MENSALISTA;

    @OneToOne(mappedBy = "presencaPartida", fetch = FetchType.LAZY)
    private EscalacaoPartidaEntity escalacao;

    private LocalDateTime respondidaEm;
}
