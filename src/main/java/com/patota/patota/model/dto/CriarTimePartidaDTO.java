package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarTimePartidaDTO(
        @NotBlank @Size(max = 50) String nome,
        @Size(max = 30) String cor) {
}
