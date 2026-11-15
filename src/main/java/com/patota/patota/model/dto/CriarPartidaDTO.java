package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Dados necessários para cadastrar uma nova partida. */
public record CriarPartidaDTO(
        @NotNull Long patotaId,
        @NotNull LocalDateTime dataHora,
        @Size(max = 1000) String observacoes) {
}
