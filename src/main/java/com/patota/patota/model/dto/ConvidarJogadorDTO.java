package com.patota.patota.model.dto;

import com.patota.patota.model.enums.ETipoJogador;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConvidarJogadorDTO(
        @NotBlank @Size(max = 120) String nome,
        @Min(1) @Max(5) Integer nota,
        ETipoJogador tipo) {
}
