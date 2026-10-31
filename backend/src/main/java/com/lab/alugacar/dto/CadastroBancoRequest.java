package com.lab.alugacar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroBancoRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "Informe o usuário")
        @Size(max = 100)
        String username,

        @NotBlank(message = "Informe a senha")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String senha,

        @NotBlank(message = "Informe o CNPJ")
        @Size(max = 18)
        String cnpj,

        @NotBlank(message = "Informe a razão social")
        @Size(max = 200)
        String razaoSocial,

        @NotBlank(message = "Informe o código do banco")
        @Size(max = 10)
        String codigoBanco
) {
}
