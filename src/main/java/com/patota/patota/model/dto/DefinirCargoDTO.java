package com.patota.patota.model.dto;

import com.patota.patota.model.enums.ECargoPatota;
import jakarta.validation.constraints.NotNull;

public record DefinirCargoDTO(@NotNull ECargoPatota cargo) {
}
