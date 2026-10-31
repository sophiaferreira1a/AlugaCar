package com.lab.alugacar.dto;

import com.lab.alugacar.model.Agente;
import com.lab.alugacar.model.Automovel;
import com.lab.alugacar.model.Banco;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.model.ContratoCredito;
import com.lab.alugacar.model.Endereco;
import com.lab.alugacar.model.Empresa;
import com.lab.alugacar.model.PedidoAluguel;
import com.lab.alugacar.model.Rendimento;
import com.lab.alugacar.model.Usuario;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static PerfilResponse perfil(Usuario usuario) {
        if (usuario instanceof Cliente cliente) {
            return new PerfilResponse(
                    cliente.getId(),
                    cliente.getNome(),
                    cliente.getUsername(),
                    cliente.getTipo(),
                    cliente.isAtivo(),
                    cliente.getDataCadastro(),
                    cliente.getRg(),
                    cliente.getCpf(),
                    cliente.getProfissao(),
                    null,
                    null,
                    null,
                    endereco(cliente.getEndereco())
            );
        }

        if (usuario instanceof Banco banco) {
            return new PerfilResponse(
                    banco.getId(),
                    banco.getNome(),
                    banco.getUsername(),
                    banco.getTipo(),
                    banco.isAtivo(),
                    banco.getDataCadastro(),
                    null,
                    null,
                    null,
                    banco.getCnpj(),
                    banco.getRazaoSocial(),
                    banco.getCodigoBanco(),
                    null
            );
        }

        if (usuario instanceof Empresa empresa) {
            return new PerfilResponse(
                    empresa.getId(),
                    empresa.getNome(),
                    empresa.getUsername(),
                    empresa.getTipo(),
                    empresa.isAtivo(),
                    empresa.getDataCadastro(),
                    null,
                    null,
                    null,
                    empresa.getCnpj(),
                    empresa.getRazaoSocial(),
                    null,
                    null
            );
        }

        if (usuario instanceof Agente agente) {
            return new PerfilResponse(
                    agente.getId(),
                    agente.getNome(),
                    agente.getUsername(),
                    agente.getTipo(),
                    agente.isAtivo(),
                    agente.getDataCadastro(),
                    null,
                    null,
                    null,
                    agente.getCnpj(),
                    agente.getRazaoSocial(),
                    null,
                    null
            );
        }

        throw new IllegalArgumentException("Tipo de usuário não suportado: " + usuario.getClass().getName());
    }

    public static EnderecoResponse endereco(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponse(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getCep()
        );
    }

    public static Endereco endereco(EnderecoRequest request) {
        if (request == null) {
            return null;
        }
        Endereco endereco = new Endereco();
        endereco.setLogradouro(request.logradouro());
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro());
        endereco.setCidade(request.cidade());
        endereco.setUf(request.uf());
        endereco.setCep(request.cep());
        return endereco;
    }

    public static RendimentoResponse rendimento(Rendimento rendimento) {
        return new RendimentoResponse(
                rendimento.getId(),
                rendimento.getEntidadeEmpregadora(),
                rendimento.getValor()
        );
    }

    public static AutomovelResponse automovel(Automovel automovel) {
        return new AutomovelResponse(
                automovel.getId(),
                automovel.getMatricula(),
                automovel.getAno(),
                automovel.getMarca(),
                automovel.getModelo(),
                automovel.getPlaca(),
                automovel.getProprietario().getId(),
                automovel.getProprietario().getNome(),
                automovel.getProprietario().getTipo()
        );
    }

    public static ContratoResponse contrato(ContratoCredito contrato) {
        if (contrato == null) {
            return null;
        }
        return new ContratoResponse(
                contrato.getId(),
                contrato.getPedido().getId(),
                contrato.getBanco().getId(),
                contrato.getBanco().getNome(),
                contrato.getValor(),
                contrato.getTaxaJuros(),
                contrato.getPrazoMeses(),
                contrato.getDataConcessao()
        );
    }

    public static PedidoResponse pedido(PedidoAluguel pedido) {
        Automovel automovel = pedido.getAutomovel();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                automovel.getId(),
                automovel.getMarca() + " " + automovel.getModelo() + " (" + automovel.getPlaca() + ")",
                pedido.getDataInicio(),
                pedido.getDataFim(),
                pedido.getStatus(),
                pedido.getObservacoes(),
                pedido.getDataCriacao(),
                pedido.getDataAtualizacao(),
                contrato(pedido.getContrato())
        );
    }
}
