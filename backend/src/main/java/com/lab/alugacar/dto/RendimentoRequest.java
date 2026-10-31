package com.lab.alugacar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RendimentoRequest(
        @NotBlank(message = "Informe a entidade empregadora")
        @Size(max = 150)
        String entidadeEmpregadora,

        @NotNull(message = "Informe o valor do rendimento")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor
) {
}
