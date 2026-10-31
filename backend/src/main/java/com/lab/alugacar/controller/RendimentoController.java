package com.lab.alugacar.controller;

import com.lab.alugacar.dto.RendimentoRequest;
import com.lab.alugacar.dto.RendimentoResponse;
import com.lab.alugacar.service.RendimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/rendimentos")
@PreAuthorize("hasRole('CLIENTE')")
public class RendimentoController {

    private final RendimentoService rendimentoService;

    public RendimentoController(RendimentoService rendimentoService) {
        this.rendimentoService = rendimentoService;
    }

    @GetMapping
    public List<RendimentoResponse> listar() {
        return rendimentoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RendimentoResponse adicionar(@Valid @RequestBody RendimentoRequest request) {
        return rendimentoService.adicionar(request);
    }

    @PutMapping("/{id}")
    public RendimentoResponse atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody RendimentoRequest request
    ) {
        return rendimentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        rendimentoService.remover(id);
    }
}
