package com.patota.patota.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalvarJogadorDTO(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String telefone,
        @Email @Size(max = 150) String email) {
}
