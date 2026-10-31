package com.lab.alugacar.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoResponse(
        UUID id,
        UUID pedidoId,
        UUID bancoId,
        String bancoNome,
        BigDecimal valor,
        BigDecimal taxaJuros,
        Integer prazoMeses,
        LocalDate dataConcessao
) {
}
