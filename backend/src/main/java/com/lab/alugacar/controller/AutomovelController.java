package com.lab.alugacar.controller;

import com.lab.alugacar.dto.AutomovelRequest;
import com.lab.alugacar.dto.AutomovelResponse;
import com.lab.alugacar.service.AutomovelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelService automovelService;

    public AutomovelController(AutomovelService automovelService) {
        this.automovelService = automovelService;
    }

    @GetMapping
    public List<AutomovelResponse> listar() {
        return automovelService.listar();
    }

    @GetMapping("/meus")
    public List<AutomovelResponse> listarMeus() {
        return automovelService.listarMeus();
    }

    @GetMapping("/{id}")
    public AutomovelResponse buscar(@PathVariable UUID id) {
        return automovelService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse criar(@Valid @RequestBody AutomovelRequest request) {
        return automovelService.criar(request);
    }

    @PutMapping("/{id}")
    public AutomovelResponse atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AutomovelRequest request
    ) {
        return automovelService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        automovelService.remover(id);
    }
}
