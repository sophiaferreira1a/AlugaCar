package com.lab.alugacar.controller;

import com.lab.alugacar.dto.AtualizarPedidoRequest;
import com.lab.alugacar.dto.AvaliacaoRequest;
import com.lab.alugacar.dto.PedidoRequest;
import com.lab.alugacar.dto.PedidoResponse;
import com.lab.alugacar.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request) {
        return pedidoService.criar(request);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PedidoResponse> listarMeus() {
        return pedidoService.listarMeus();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENTE', 'BANCO')")
    public List<PedidoResponse> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable UUID id) {
        return pedidoService.buscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarPedidoRequest request
    ) {
        return pedidoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse cancelar(@PathVariable UUID id) {
        return pedidoService.cancelar(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENTE', 'BANCO')")
    public PedidoResponse avaliar(
            @PathVariable UUID id,
            @Valid @RequestBody AvaliacaoRequest request
    ) {
        return pedidoService.avaliar(id, request);
    }
}
