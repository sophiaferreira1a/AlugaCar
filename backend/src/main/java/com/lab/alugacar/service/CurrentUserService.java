package com.lab.alugacar.service;

import com.lab.alugacar.exception.ForbiddenException;
import com.lab.alugacar.exception.UnauthorizedException;
import com.lab.alugacar.model.Agente;
import com.lab.alugacar.model.Banco;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.model.Usuario;
import com.lab.alugacar.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }

    public Cliente clienteAtual() {
        Usuario usuario = usuarioAtual();
        if (usuario instanceof Cliente cliente) {
            return cliente;
        }
        throw new ForbiddenException("Esta operação é exclusiva de clientes");
    }

    public Agente agenteAtual() {
        Usuario usuario = usuarioAtual();
        if (usuario instanceof Agente agente) {
            return agente;
        }
        throw new ForbiddenException("Esta operação é exclusiva de agentes");
    }

    public Banco bancoAtual() {
        Usuario usuario = usuarioAtual();
        if (usuario instanceof Banco banco) {
            return banco;
        }
        throw new ForbiddenException("Esta operação é exclusiva de bancos");
    }
}
