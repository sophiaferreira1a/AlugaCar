package com.lab.alugacar.repository;

import com.lab.alugacar.model.Rendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RendimentoRepository extends JpaRepository<Rendimento, UUID> {

    List<Rendimento> findByClienteIdOrderById(UUID clienteId);

    Optional<Rendimento> findByIdAndClienteId(UUID id, UUID clienteId);

    long countByClienteId(UUID clienteId);
}
