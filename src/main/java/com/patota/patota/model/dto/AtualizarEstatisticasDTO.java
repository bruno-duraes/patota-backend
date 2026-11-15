package com.patota.patota.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarEstatisticasDTO(
        @NotNull @Min(0) Integer gols,
        @NotNull @Min(0) Integer assistencias) {
}
