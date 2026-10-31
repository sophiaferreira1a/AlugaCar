package com.lab.alugacar.dto;

import com.lab.alugacar.model.StatusPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "Informe o novo status")
        StatusPedido status,

        @Size(max = 2000)
        String observacoes
) {
}
