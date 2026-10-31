package com.lab.alugacar.repository;

import com.lab.alugacar.model.ContratoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContratoCreditoRepository extends JpaRepository<ContratoCredito, UUID> {

    Optional<ContratoCredito> findByPedidoId(UUID pedidoId);

    boolean existsByPedidoId(UUID pedidoId);
}
