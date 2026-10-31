package com.lab.alugacar.repository;

import com.lab.alugacar.model.Agente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgenteRepository extends JpaRepository<Agente, UUID> {

    Optional<Agente> findByUsername(String username);

    boolean existsByCnpj(String cnpj);
}
