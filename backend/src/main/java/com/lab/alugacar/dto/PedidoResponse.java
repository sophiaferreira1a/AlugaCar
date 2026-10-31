package com.lab.alugacar.dto;

import com.lab.alugacar.model.StatusPedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        UUID clienteId,
        String clienteNome,
        UUID automovelId,
        String automovelDescricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusPedido status,
        String observacoes,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        ContratoResponse contrato
) {
}
