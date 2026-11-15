package com.patota.patota.model.dto;

import com.patota.patota.model.enums.ETipoJogador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarJogadorDTO(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Min(1) @Max(5) Integer nota,
        ETipoJogador tipo) {
}
