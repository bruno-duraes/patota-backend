package com.patota.patota.model.dto;

import java.time.LocalDateTime;

/** Dados necessários para cadastrar uma nova partida. */
public record CriarPartidaDTO(
        Long patotaId,
        LocalDateTime dataHora,
        String observacoes) {
}
