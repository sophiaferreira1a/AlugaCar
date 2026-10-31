package com.lab.alugacar.controller;

import com.lab.alugacar.dto.CadastroBancoRequest;
import com.lab.alugacar.dto.CadastroClienteRequest;
import com.lab.alugacar.dto.CadastroEmpresaRequest;
import com.lab.alugacar.dto.LoginRequest;
import com.lab.alugacar.dto.LoginResponse;
import com.lab.alugacar.dto.PerfilResponse;
import com.lab.alugacar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register/cliente")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse cadastrarCliente(@Valid @RequestBody CadastroClienteRequest request) {
        return authService.cadastrarCliente(request);
    }

    @PostMapping("/register/empresa")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse cadastrarEmpresa(@Valid @RequestBody CadastroEmpresaRequest request) {
        return authService.cadastrarEmpresa(request);
    }

    @PostMapping("/register/banco")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse cadastrarBanco(@Valid @RequestBody CadastroBancoRequest request) {
        return authService.cadastrarBanco(request);
    }
}
