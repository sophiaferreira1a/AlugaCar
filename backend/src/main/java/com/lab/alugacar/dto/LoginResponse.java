package com.lab.alugacar.dto;

public record LoginResponse(
        String token,
        PerfilResponse usuario
) {
}
