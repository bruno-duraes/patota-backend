package com.patota.patota.model.dto;

import com.patota.patota.model.entity.ParticipanteDaPartida;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoParticipantePartida;
import com.patota.patota.model.enums.ETipoJogador;

public record ParticipanteTimeRespostaDTO(
        ETipoParticipantePartida tipo, Long id, String nome, int nota, ETipoJogador tipoJogador,
        EStatusParticipante status, Long timeId, int gols, int assistencias) {

    public static ParticipanteTimeRespostaDTO de(
            ParticipanteDaPartida participante, ETipoParticipantePartida tipo, String nome) {
        return new ParticipanteTimeRespostaDTO(tipo, participante.getId(), nome,
                participante.getNota(), participante.getTipo(),
                participante.getStatus(),
                participante.getTimePartida() == null ? null : participante.getTimePartida().getId(),
                participante.getGols(), participante.getAssistencias());
    }
}
