package com.lab.alugacar.repository;

import com.lab.alugacar.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BancoRepository extends JpaRepository<Banco, UUID> {

    Optional<Banco> findByUsername(String username);

    boolean existsByCnpj(String cnpj);

    boolean existsByCodigoBanco(String codigoBanco);
}
