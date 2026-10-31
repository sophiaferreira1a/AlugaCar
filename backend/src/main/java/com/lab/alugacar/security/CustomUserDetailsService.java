package com.lab.alugacar.security;

import com.lab.alugacar.model.Usuario;
import com.lab.alugacar.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenha())
                .authorities(authorities(usuario))
                .disabled(!usuario.isAtivo())
                .build();
    }

    public static List<GrantedAuthority> authorities(Usuario usuario) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        switch (usuario.getTipo()) {
            case CLIENTE -> authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            case EMPRESA -> authorities.add(new SimpleGrantedAuthority("ROLE_AGENTE"));
            case BANCO -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_AGENTE"));
                authorities.add(new SimpleGrantedAuthority("ROLE_BANCO"));
            }
        }
        return authorities;
    }
}
