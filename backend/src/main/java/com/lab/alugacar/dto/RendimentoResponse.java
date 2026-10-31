package com.lab.alugacar.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RendimentoResponse(
        UUID id,
        String entidadeEmpregadora,
        BigDecimal valor
) {
}
