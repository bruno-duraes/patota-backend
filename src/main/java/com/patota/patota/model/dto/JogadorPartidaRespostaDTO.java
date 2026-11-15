package com.patota.patota.model.dto;

import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import java.time.LocalDateTime;

public record JogadorPartidaRespostaDTO(
        Long id, Long jogadorId, String nome, EStatusParticipante status, LocalDateTime respondidaEm) {

    public static JogadorPartidaRespostaDTO de(JogadorPartidaEntity presenca) {
        return new JogadorPartidaRespostaDTO(presenca.getId(), presenca.getJogador().getId(),
                presenca.getJogador().getNome(), presenca.getStatus(), presenca.getRespondidaEm());
    }
}
