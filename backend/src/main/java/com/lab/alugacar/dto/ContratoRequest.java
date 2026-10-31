package com.lab.alugacar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ContratoRequest(
        @NotNull(message = "Informe o pedido")
        UUID pedidoId,

        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Informe a taxa de juros")
        @DecimalMin(value = "0.00", message = "A taxa não pode ser negativa")
        BigDecimal taxaJuros,

        @NotNull(message = "Informe o prazo em meses")
        @Min(value = 1, message = "O prazo deve ser de pelo menos 1 mês")
        Integer prazoMeses
) {
}
