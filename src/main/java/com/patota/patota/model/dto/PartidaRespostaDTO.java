package com.patota.patota.model.dto;

import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.enums.EStatusPartida;
import java.time.LocalDateTime;

public record PartidaRespostaDTO(
        Long id, Long patotaId, LocalDateTime dataHora, String local, int maximoJogadores,
        String observacoes, EStatusPartida status, LocalDateTime criadoEm) {

    public static PartidaRespostaDTO de(PartidaEntity partida) {
        return new PartidaRespostaDTO(partida.getId(), partida.getPatota().getId(),
                partida.getDataHora(), partida.getLocal(), partida.getMaximoJogadores(),
                partida.getObservacoes(),
                partida.getStatus(), partida.getCriadoEm());
    }
}
