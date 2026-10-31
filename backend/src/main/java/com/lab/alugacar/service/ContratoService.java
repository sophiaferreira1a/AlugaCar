package com.lab.alugacar.service;

import com.lab.alugacar.dto.ContratoRequest;
import com.lab.alugacar.dto.ContratoResponse;
import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.exception.BusinessException;
import com.lab.alugacar.exception.ConflictException;
import com.lab.alugacar.exception.ResourceNotFoundException;
import com.lab.alugacar.model.Banco;
import com.lab.alugacar.model.ContratoCredito;
import com.lab.alugacar.model.PedidoAluguel;
import com.lab.alugacar.model.StatusPedido;
import com.lab.alugacar.repository.ContratoCreditoRepository;
import com.lab.alugacar.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoService {

    private final CurrentUserService currentUserService;
    private final PedidoAluguelRepository pedidoRepository;
    private final ContratoCreditoRepository contratoRepository;

    public ContratoService(
            CurrentUserService currentUserService,
            PedidoAluguelRepository pedidoRepository,
            ContratoCreditoRepository contratoRepository
    ) {
        this.currentUserService = currentUserService;
        this.pedidoRepository = pedidoRepository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional
    public ContratoResponse criar(ContratoRequest request) {
        Banco banco = currentUserService.bancoAtual();
        PedidoAluguel pedido = pedidoRepository.findById(request.pedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (pedido.getStatus() != StatusPedido.APROVADO) {
            throw new BusinessException("O contrato só pode ser criado para pedidos aprovados");
        }
        if (contratoRepository.existsByPedidoId(pedido.getId())) {
            throw new ConflictException("Este pedido já possui um contrato de crédito");
        }

        ContratoCredito contrato = new ContratoCredito();
        contrato.setPedido(pedido);
        contrato.setBanco(banco);
        contrato.setValor(request.valor());
        contrato.setTaxaJuros(request.taxaJuros());
        contrato.setPrazoMeses(request.prazoMeses());
        contratoRepository.save(contrato);

        pedido.setStatus(StatusPedido.CONTRATO_EXECUTADO);
        pedido.setContrato(contrato);
        pedidoRepository.save(pedido);

        return DtoMapper.contrato(contrato);
    }
}
