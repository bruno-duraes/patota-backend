package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConvidarJogadorDTO(@NotBlank @Size(max = 120) String nome) {
}
