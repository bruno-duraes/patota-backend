package com.patota.patota.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Um dos times formados para uma partida. */
@Entity
@Table(
        name = "times_partida",
        uniqueConstraints = @UniqueConstraint(name = "uk_partida_nome_time", columnNames = {"partida_id", "nome"}))
@Getter
@Setter
@NoArgsConstructor
public class TimePartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 30)
    private String cor;

    @Min(0)
    @Column(nullable = false)
    private int placar = 0;

    @OneToMany(mappedBy = "timePartida")
    private List<EscalacaoPartidaEntity> escalacoes = new ArrayList<>();
}
