package com.lab.alugacar.controller;

import com.lab.alugacar.dto.AtualizarClienteRequest;
import com.lab.alugacar.dto.PerfilResponse;
import com.lab.alugacar.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse obter() {
        return perfilService.obter();
    }

    @PutMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public PerfilResponse atualizar(@Valid @RequestBody AtualizarClienteRequest request) {
        return perfilService.atualizarCliente(request);
    }
}
