package com.lab.alugacar.service;

import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.dto.RendimentoRequest;
import com.lab.alugacar.dto.RendimentoResponse;
import com.lab.alugacar.exception.BusinessException;
import com.lab.alugacar.exception.ResourceNotFoundException;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.model.Rendimento;
import com.lab.alugacar.repository.RendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RendimentoService {

    private static final int LIMITE_RENDIMENTOS = 3;

    private final CurrentUserService currentUserService;
    private final RendimentoRepository rendimentoRepository;

    public RendimentoService(CurrentUserService currentUserService, RendimentoRepository rendimentoRepository) {
        this.currentUserService = currentUserService;
        this.rendimentoRepository = rendimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<RendimentoResponse> listar() {
        Cliente cliente = currentUserService.clienteAtual();
        return rendimentoRepository.findByClienteIdOrderById(cliente.getId())
                .stream()
                .map(DtoMapper::rendimento)
                .toList();
    }

    @Transactional
    public RendimentoResponse adicionar(RendimentoRequest request) {
        Cliente cliente = currentUserService.clienteAtual();
        if (rendimentoRepository.countByClienteId(cliente.getId()) >= LIMITE_RENDIMENTOS) {
            throw new BusinessException("O cliente pode ter no máximo 3 rendimentos");
        }

        Rendimento rendimento = new Rendimento();
        rendimento.setCliente(cliente);
        rendimento.setEntidadeEmpregadora(request.entidadeEmpregadora().trim());
        rendimento.setValor(request.valor());
        return DtoMapper.rendimento(rendimentoRepository.save(rendimento));
    }

    @Transactional
    public RendimentoResponse atualizar(UUID id, RendimentoRequest request) {
        Cliente cliente = currentUserService.clienteAtual();
        Rendimento rendimento = rendimentoRepository.findByIdAndClienteId(id, cliente.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rendimento não encontrado"));
        rendimento.setEntidadeEmpregadora(request.entidadeEmpregadora().trim());
        rendimento.setValor(request.valor());
        return DtoMapper.rendimento(rendimentoRepository.save(rendimento));
    }

    @Transactional
    public void remover(UUID id) {
        Cliente cliente = currentUserService.clienteAtual();
        Rendimento rendimento = rendimentoRepository.findByIdAndClienteId(id, cliente.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rendimento não encontrado"));
        rendimentoRepository.delete(rendimento);
    }
}
