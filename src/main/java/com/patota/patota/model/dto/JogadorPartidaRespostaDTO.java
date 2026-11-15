package com.patota.patota.model.dto;

import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoJogador;
import java.time.LocalDateTime;

public record JogadorPartidaRespostaDTO(
        Long id, Long jogadorId, String nome, int nota, ETipoJogador tipo, EStatusParticipante status,
        LocalDateTime respondidaEm, Long timePartidaId, int gols, int assistencias) {

    public static JogadorPartidaRespostaDTO de(JogadorPartidaEntity participante) {
        return new JogadorPartidaRespostaDTO(participante.getId(), participante.getJogador().getId(),
                participante.getJogador().getNome(), participante.getNota(), participante.getTipo(),
                participante.getStatus(), participante.getRespondidaEm(),
                participante.getTimePartida() == null ? null : participante.getTimePartida().getId(),
                participante.getGols(), participante.getAssistencias());
    }
}
