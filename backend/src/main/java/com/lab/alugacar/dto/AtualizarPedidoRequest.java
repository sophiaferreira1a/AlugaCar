package com.lab.alugacar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtualizarPedidoRequest(
        @NotNull(message = "Informe a data de início")
        LocalDate dataInicio,

        @NotNull(message = "Informe a data de fim")
        LocalDate dataFim,

        @Size(max = 2000)
        String observacoes
) {
}
