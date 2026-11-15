package com.patota.patota.model.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarMensalistaDTO(@NotNull Long jogadorId) {
}
