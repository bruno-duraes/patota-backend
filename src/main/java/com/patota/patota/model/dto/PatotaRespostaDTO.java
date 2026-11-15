package com.patota.patota.model.dto;

import com.patota.patota.model.entity.PatotaEntity;
import java.time.LocalDateTime;

public record PatotaRespostaDTO(
        Long id, String nome, String descricao, String local, int maximoJogadoresPorPartida, boolean ativa,
        LocalDateTime criadoEm, LocalDateTime atualizadoEm) {

    public static PatotaRespostaDTO de(PatotaEntity patota) {
        return new PatotaRespostaDTO(patota.getId(), patota.getNome(), patota.getDescricao(),
                patota.getLocal(), patota.getMaximoJogadoresPorPartida(), patota.isAtiva(),
                patota.getCriadoEm(), patota.getAtualizadoEm());
    }
}
