package com.patota.patota.model.dto;

import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoJogador;
import java.time.LocalDateTime;

public record PartidaConvidadoRespostaDTO(
        Long id, String nome, int nota, ETipoJogador tipo, Long convidadoPorJogadorId,
        EStatusParticipante status, LocalDateTime respondidaEm,
        Long timePartidaId, int gols, int assistencias) {

    public static PartidaConvidadoRespostaDTO de(PartidaConvidadoEntity participante) {
        return new PartidaConvidadoRespostaDTO(participante.getId(), participante.getNome(),
                participante.getNota(), participante.getTipo(),
                participante.getConvidadoPorJogador().getId(), participante.getStatus(),
                participante.getRespondidaEm(),
                participante.getTimePartida() == null ? null : participante.getTimePartida().getId(),
                participante.getGols(), participante.getAssistencias());
    }
}
