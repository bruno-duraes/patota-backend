package com.patota.patota.model.entity;

import com.patota.patota.model.enums.EStatusPartida;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "partidas")
@Getter
@Setter
@NoArgsConstructor
public class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patota_id", nullable = false)
    private PatotaEntity patota;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Endereço copiado da patota no momento de criação da partida. */
    @Column(nullable = false, length = 255)
    private String local;

    @Column(length = 1000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EStatusPartida status = EStatusPartida.AGENDADA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }
}
