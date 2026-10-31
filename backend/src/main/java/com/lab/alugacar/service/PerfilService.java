package com.lab.alugacar.service;

import com.lab.alugacar.dto.AtualizarClienteRequest;
import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.dto.PerfilResponse;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final CurrentUserService currentUserService;
    private final ClienteRepository clienteRepository;

    public PerfilService(CurrentUserService currentUserService, ClienteRepository clienteRepository) {
        this.currentUserService = currentUserService;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obter() {
        return DtoMapper.perfil(currentUserService.usuarioAtual());
    }

    @Transactional
    public PerfilResponse atualizarCliente(AtualizarClienteRequest request) {
        Cliente cliente = currentUserService.clienteAtual();
        cliente.setNome(request.nome().trim());
        cliente.setProfissao(request.profissao());
        if (request.endereco() != null) {
            cliente.setEndereco(DtoMapper.endereco(request.endereco()));
        }
        return DtoMapper.perfil(clienteRepository.save(cliente));
    }
}
