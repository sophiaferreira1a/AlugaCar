package com.lab.alugacar.dto;

import com.lab.alugacar.model.TipoUsuario;

import java.util.UUID;

public record AutomovelResponse(
        UUID id,
        String matricula,
        Integer ano,
        String marca,
        String modelo,
        String placa,
        UUID proprietarioId,
        String proprietarioNome,
        TipoUsuario proprietarioTipo
) {
}
