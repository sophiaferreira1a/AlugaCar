package com.lab.alugacar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CadastroClienteRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "Informe o usuário")
        @Size(max = 100)
        String username,

        @NotBlank(message = "Informe a senha")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String senha,

        @NotBlank(message = "Informe o RG")
        @Size(max = 20)
        String rg,

        @NotBlank(message = "Informe o CPF")
        @Size(max = 14)
        String cpf,

        @Size(max = 100)
        String profissao,

        @Valid
        EnderecoRequest endereco,

        @Size(max = 3, message = "Informe no máximo 3 rendimentos")
        List<@Valid RendimentoRequest> rendimentos
) {
}
