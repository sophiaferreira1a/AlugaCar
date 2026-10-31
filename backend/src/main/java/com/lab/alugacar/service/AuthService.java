package com.lab.alugacar.service;

import com.lab.alugacar.dto.CadastroBancoRequest;
import com.lab.alugacar.dto.CadastroClienteRequest;
import com.lab.alugacar.dto.CadastroEmpresaRequest;
import com.lab.alugacar.dto.DtoMapper;
import com.lab.alugacar.dto.LoginRequest;
import com.lab.alugacar.dto.LoginResponse;
import com.lab.alugacar.dto.PerfilResponse;
import com.lab.alugacar.dto.RendimentoRequest;
import com.lab.alugacar.exception.ConflictException;
import com.lab.alugacar.exception.UnauthorizedException;
import com.lab.alugacar.model.Banco;
import com.lab.alugacar.model.Cliente;
import com.lab.alugacar.model.Empresa;
import com.lab.alugacar.model.Rendimento;
import com.lab.alugacar.model.Usuario;
import com.lab.alugacar.repository.BancoRepository;
import com.lab.alugacar.repository.ClienteRepository;
import com.lab.alugacar.repository.EmpresaRepository;
import com.lab.alugacar.repository.UsuarioRepository;
import com.lab.alugacar.security.CustomUserDetailsService;
import com.lab.alugacar.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final BancoRepository bancoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            EmpresaRepository empresaRepository,
            BancoRepository bancoRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.bancoRepository = bancoRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public PerfilResponse cadastrarCliente(CadastroClienteRequest request) {
        String username = request.username().trim();
        String cpf = request.cpf().trim();
        validarUsername(username);
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ConflictException("Já existe um cliente com este CPF");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome().trim());
        cliente.setUsername(username);
        cliente.setSenha(passwordEncoder.encode(request.senha()));
        cliente.setRg(request.rg().trim());
        cliente.setCpf(cpf);
        cliente.setProfissao(request.profissao());
        cliente.setEndereco(DtoMapper.endereco(request.endereco()));

        List<RendimentoRequest> rendimentos = request.rendimentos() == null
                ? List.of()
                : request.rendimentos();
        for (RendimentoRequest rendimentoRequest : rendimentos) {
            Rendimento rendimento = new Rendimento();
            rendimento.setEntidadeEmpregadora(rendimentoRequest.entidadeEmpregadora().trim());
            rendimento.setValor(rendimentoRequest.valor());
            cliente.adicionarRendimento(rendimento);
        }

        return DtoMapper.perfil(clienteRepository.save(cliente));
    }

    @Transactional
    public PerfilResponse cadastrarEmpresa(CadastroEmpresaRequest request) {
        String username = request.username().trim();
        String cnpj = request.cnpj().trim();
        validarUsername(username);
        if (empresaRepository.existsByCnpj(cnpj)) {
            throw new ConflictException("Já existe uma empresa com este CNPJ");
        }

        Empresa empresa = new Empresa();
        empresa.setNome(request.nome().trim());
        empresa.setUsername(username);
        empresa.setSenha(passwordEncoder.encode(request.senha()));
        empresa.setCnpj(cnpj);
        empresa.setRazaoSocial(request.razaoSocial().trim());

        return DtoMapper.perfil(empresaRepository.save(empresa));
    }

    @Transactional
    public PerfilResponse cadastrarBanco(CadastroBancoRequest request) {
        String username = request.username().trim();
        String cnpj = request.cnpj().trim();
        String codigoBanco = request.codigoBanco().trim();
        validarUsername(username);
        if (bancoRepository.existsByCnpj(cnpj)) {
            throw new ConflictException("Já existe um banco com este CNPJ");
        }
        if (bancoRepository.existsByCodigoBanco(codigoBanco)) {
            throw new ConflictException("Já existe um banco com este código");
        }

        Banco banco = new Banco();
        banco.setNome(request.nome().trim());
        banco.setUsername(username);
        banco.setSenha(passwordEncoder.encode(request.senha()));
        banco.setCnpj(cnpj);
        banco.setRazaoSocial(request.razaoSocial().trim());
        banco.setCodigoBanco(codigoBanco);

        return DtoMapper.perfil(bancoRepository.save(banco));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.senha()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Usuário ou senha inválidos");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Usuário ou senha inválidos"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.gerarToken(userDetails);
        return new LoginResponse(token, DtoMapper.perfil(usuario));
    }

    private void validarUsername(String username) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictException("Já existe um usuário com este nome de acesso");
        }
    }
}
