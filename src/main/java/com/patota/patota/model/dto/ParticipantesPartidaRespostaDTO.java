package com.patota.patota.model.dto;

import java.util.List;

public record ParticipantesPartidaRespostaDTO(
        List<JogadorPartidaRespostaDTO> mensalistas,
        List<PartidaConvidadoRespostaDTO> convidados) {
}
