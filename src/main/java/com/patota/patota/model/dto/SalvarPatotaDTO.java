package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SalvarPatotaDTO(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 500) String descricao,
        @NotBlank @Size(max = 255) String local,
        @Min(2) @Max(100) Integer maximoJogadoresPorPartida) {
}
