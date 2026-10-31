package com.lab.alugacar.service;

import com.lab.alugacar.dto.AtualizarPedidoRequest;
import com.lab.alugacar.dto.AvaliacaoRequest;
import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.dto.PedidoRequest;
import com.lab.alugacar.dto.PedidoResponse;
import com.lab.alugacar.exception.BusinessException;
import com.lab.alugacar.exception.ForbiddenException;
import com.lab.alugacar.exception.ResourceNotFoundException;
import com.lab.alugacar.model.Agente;
import com.lab.alugacar.model.Automovel;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.model.PedidoAluguel;
import com.lab.alugacar.model.StatusPedido;
import com.lab.alugacar.model.Usuario;
import com.lab.alugacar.repository.AutomovelRepository;
import com.lab.alugacar.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PedidoService {

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_AGENTE = Map.of(
            StatusPedido.PENDENTE, Set.of(StatusPedido.EM_ANALISE),
            StatusPedido.EM_ANALISE, Set.of(StatusPedido.APROVADO, StatusPedido.REPROVADO)
    );

    private static final Set<StatusPedido> STATUS_FINAIS = Set.of(
            StatusPedido.REPROVADO,
            StatusPedido.CANCELADO,
            StatusPedido.CONTRATO_EXECUTADO
    );

    private final CurrentUserService currentUserService;
    private final PedidoAluguelRepository pedidoRepository;
    private final AutomovelRepository automovelRepository;

    public PedidoService(
            CurrentUserService currentUserService,
            PedidoAluguelRepository pedidoRepository,
            AutomovelRepository automovelRepository
    ) {
        this.currentUserService = currentUserService;
        this.pedidoRepository = pedidoRepository;
        this.automovelRepository = automovelRepository;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Cliente cliente = currentUserService.clienteAtual();
        Automovel automovel = automovelRepository.findById(request.automovelId())
                .orElseThrow(() -> new ResourceNotFoundException("Automóvel não encontrado"));
        validarDatas(request.dataInicio(), request.dataFim());

        PedidoAluguel pedido = new PedidoAluguel();
        pedido.setCliente(cliente);
        pedido.setAutomovel(automovel);
        pedido.setDataInicio(request.dataInicio());
        pedido.setDataFim(request.dataFim());
        pedido.setObservacoes(request.observacoes());
        pedido.setStatus(StatusPedido.PENDENTE);

        return DtoMapper.pedido(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMeus() {
        Cliente cliente = currentUserService.clienteAtual();
        return pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(cliente.getId())
                .stream()
                .map(DtoMapper::pedido)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        currentUserService.agenteAtual();
        return pedidoRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(DtoMapper::pedido)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(UUID id) {
        return DtoMapper.pedido(buscarComAcesso(id));
    }

    @Transactional
    public PedidoResponse atualizar(UUID id, AtualizarPedidoRequest request) {
        Cliente cliente = currentUserService.clienteAtual();
        PedidoAluguel pedido = buscarEntidade(id);
        validarClienteDoPedido(pedido, cliente);
        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.EM_ANALISE) {
            throw new BusinessException("O pedido só pode ser alterado enquanto estiver pendente ou em análise");
        }
        validarDatas(request.dataInicio(), request.dataFim());

        pedido.setDataInicio(request.dataInicio());
        pedido.setDataFim(request.dataFim());
        pedido.setObservacoes(request.observacoes());
        return DtoMapper.pedido(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelar(UUID id) {
        Cliente cliente = currentUserService.clienteAtual();
        PedidoAluguel pedido = buscarEntidade(id);
        validarClienteDoPedido(pedido, cliente);
        if (STATUS_FINAIS.contains(pedido.getStatus())) {
            throw new BusinessException("Este pedido não pode mais ser cancelado");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return DtoMapper.pedido(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse avaliar(UUID id, AvaliacaoRequest request) {
        currentUserService.agenteAtual();
        PedidoAluguel pedido = buscarEntidade(id);
        StatusPedido novoStatus = request.status();

        if (!Set.of(StatusPedido.EM_ANALISE, StatusPedido.APROVADO, StatusPedido.REPROVADO)
                .contains(novoStatus)) {
            throw new BusinessException("O agente só pode mover o pedido para EM_ANALISE, APROVADO ou REPROVADO");
        }

        Set<StatusPedido> permitidos = TRANSICOES_AGENTE.getOrDefault(pedido.getStatus(), Set.of());
        if (!permitidos.contains(novoStatus)) {
            throw new BusinessException("Transição de status inválida: "
                    + pedido.getStatus() + " -> " + novoStatus);
        }

        pedido.setStatus(novoStatus);
        if (request.observacoes() != null) {
            pedido.setObservacoes(request.observacoes());
        }
        return DtoMapper.pedido(pedidoRepository.save(pedido));
    }

    private PedidoAluguel buscarComAcesso(UUID id) {
        PedidoAluguel pedido = buscarEntidade(id);
        Usuario usuario = currentUserService.usuarioAtual();
        if (usuario instanceof Cliente cliente) {
            validarClienteDoPedido(pedido, cliente);
        } else if (!(usuario instanceof Agente)) {
            throw new ForbiddenException("Você não pode acessar este pedido");
        }
        return pedido;
    }

    private PedidoAluguel buscarEntidade(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    private void validarClienteDoPedido(PedidoAluguel pedido, Cliente cliente) {
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new ForbiddenException("Você só pode acessar pedidos de sua autoria");
        }
    }

    private void validarDatas(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data de fim não pode ser anterior à data de início");
        }
    }
}
