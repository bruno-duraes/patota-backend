package com.patota.patota.model.dto;

import com.patota.patota.model.entity.TimePartidaEntity;
import java.util.List;

public record TimePartidaRespostaDTO(
        Long id, Long partidaId, String nome, String cor, int placar,
        List<ParticipanteTimeRespostaDTO> participantes) {

    public static TimePartidaRespostaDTO de(
            TimePartidaEntity time, List<ParticipanteTimeRespostaDTO> participantes) {
        return new TimePartidaRespostaDTO(time.getId(), time.getPartida().getId(),
                time.getNome(), time.getCor(), time.getPlacar(), participantes);
    }
}
