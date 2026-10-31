package com.lab.alugacar.dto;

import com.lab.alugacar.model.TipoUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record PerfilResponse(
        UUID id,
        String nome,
        String username,
        TipoUsuario tipo,
        boolean ativo,
        LocalDateTime dataCadastro,
        String rg,
        String cpf,
        String profissao,
        String cnpj,
        String razaoSocial,
        String codigoBanco,
        EnderecoResponse endereco
) {
}
