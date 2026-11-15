package com.patota.patota.model.dto;

import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.enums.ECargoPatota;
import java.time.LocalDateTime;

public record MensalistaRespostaDTO(
        Long id, Long patotaId, Long jogadorId, boolean ativo, ECargoPatota cargo,
        LocalDateTime entrouEm, LocalDateTime saiuEm) {

    public static MensalistaRespostaDTO de(PatotaJogadorEntity mensalista) {
        return new MensalistaRespostaDTO(mensalista.getId(), mensalista.getPatota().getId(),
                mensalista.getJogador().getId(), mensalista.isAtivo(), mensalista.getCargo(),
                mensalista.getEntrouEm(), mensalista.getSaiuEm());
    }
}
