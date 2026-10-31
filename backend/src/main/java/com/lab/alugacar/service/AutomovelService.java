package com.lab.alugacar.service;

import com.lab.alugacar.dto.AutomovelRequest;
import com.lab.alugacar.dto.AutomovelResponse;
import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.exception.ConflictException;
import com.lab.alugacar.exception.ForbiddenException;
import com.lab.alugacar.exception.ResourceNotFoundException;
import com.lab.alugacar.model.Automovel;
import com.lab.alugacar.model.Usuario;
import com.lab.alugacar.repository.AutomovelRepository;
import com.lab.alugacar.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AutomovelService {

    private final CurrentUserService currentUserService;
    private final AutomovelRepository automovelRepository;
    private final PedidoAluguelRepository pedidoRepository;

    public AutomovelService(
            CurrentUserService currentUserService,
            AutomovelRepository automovelRepository,
            PedidoAluguelRepository pedidoRepository
    ) {
        this.currentUserService = currentUserService;
        this.automovelRepository = automovelRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automovelRepository.findAllByOrderByMarcaAscModeloAsc()
                .stream()
                .map(DtoMapper::automovel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listarMeus() {
        Usuario usuario = currentUserService.usuarioAtual();
        return automovelRepository.findByProprietarioIdOrderByMarcaAscModeloAsc(usuario.getId())
                .stream()
                .map(DtoMapper::automovel)
                .toList();
    }

    @Transactional(readOnly = true)
    public AutomovelResponse buscar(UUID id) {
        return DtoMapper.automovel(buscarEntidade(id));
    }

    @Transactional
    public AutomovelResponse criar(AutomovelRequest request) {
        String matricula = request.matricula().trim();
        String placa = request.placa().trim().toUpperCase();
        if (automovelRepository.existsByMatricula(matricula)) {
            throw new ConflictException("Já existe um automóvel com esta matrícula");
        }
        if (automovelRepository.existsByPlaca(placa)) {
            throw new ConflictException("Já existe um automóvel com esta placa");
        }

        Automovel automovel = new Automovel();
        automovel.setMatricula(matricula);
        automovel.setAno(request.ano());
        automovel.setMarca(request.marca().trim());
        automovel.setModelo(request.modelo().trim());
        automovel.setPlaca(placa);
        automovel.setProprietario(currentUserService.usuarioAtual());
        return DtoMapper.automovel(automovelRepository.save(automovel));
    }

    @Transactional
    public AutomovelResponse atualizar(UUID id, AutomovelRequest request) {
        Automovel automovel = buscarEntidade(id);
        validarPropriedade(automovel);

        String matricula = request.matricula().trim();
        String placa = request.placa().trim().toUpperCase();
        if (automovelRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new ConflictException("Já existe um automóvel com esta matrícula");
        }
        if (automovelRepository.existsByPlacaAndIdNot(placa, id)) {
            throw new ConflictException("Já existe um automóvel com esta placa");
        }

        automovel.setMatricula(matricula);
        automovel.setAno(request.ano());
        automovel.setMarca(request.marca().trim());
        automovel.setModelo(request.modelo().trim());
        automovel.setPlaca(placa);
        return DtoMapper.automovel(automovelRepository.save(automovel));
    }

    @Transactional
    public void remover(UUID id) {
        Automovel automovel = buscarEntidade(id);
        validarPropriedade(automovel);
        if (pedidoRepository.existsByAutomovelId(id)) {
            throw new ConflictException("Não é possível remover um automóvel que já possui pedidos");
        }
        automovelRepository.delete(automovel);
    }

    private Automovel buscarEntidade(UUID id) {
        return automovelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automóvel não encontrado"));
    }

    private void validarPropriedade(Automovel automovel) {
        Usuario usuario = currentUserService.usuarioAtual();
        if (!automovel.getProprietario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você só pode alterar automóveis de sua propriedade");
        }
    }
}
