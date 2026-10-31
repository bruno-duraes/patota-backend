package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalvarPatotaDTO(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 500) String descricao,
        @NotBlank @Size(max = 255) String local) {
}
