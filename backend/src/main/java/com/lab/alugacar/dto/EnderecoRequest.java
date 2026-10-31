package com.lab.alugacar.dto;

import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @Size(max = 150) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @Size(max = 100) String bairro,
        @Size(max = 100) String cidade,
        @Size(max = 2) String uf,
        @Size(max = 10) String cep
) {
}
