package com.lab.alugacar.repository;

import com.lab.alugacar.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    Optional<Empresa> findByUsername(String username);

    boolean existsByCnpj(String cnpj);
}
