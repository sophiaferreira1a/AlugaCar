package com.lab.alugacar.repository;

import com.lab.alugacar.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByUsername(String username);

    boolean existsByCpf(String cpf);
}
