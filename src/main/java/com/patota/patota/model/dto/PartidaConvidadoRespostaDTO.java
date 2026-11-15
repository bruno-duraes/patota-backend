package com.patota.patota.model.dto;

import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import java.time.LocalDateTime;

public record PartidaConvidadoRespostaDTO(
        Long id, String nome, Long convidadoPorJogadorId,
        EStatusParticipante status, LocalDateTime respondidaEm) {

    public static PartidaConvidadoRespostaDTO de(PartidaConvidadoEntity presenca) {
        return new PartidaConvidadoRespostaDTO(presenca.getId(), presenca.getNome(),
                presenca.getConvidadoPorJogador().getId(), presenca.getStatus(), presenca.getRespondidaEm());
    }
}
