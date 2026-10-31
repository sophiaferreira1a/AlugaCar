package com.lab.alugacar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarClienteRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 150)
        String nome,

        @Size(max = 100)
        String profissao,

        @Valid
        EnderecoRequest endereco
) {
}
