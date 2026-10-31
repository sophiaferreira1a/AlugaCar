package com.lab.alugacar.repository;

import com.lab.alugacar.model.PedidoAluguel;
import com.lab.alugacar.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, UUID> {

    List<PedidoAluguel> findAllByOrderByDataCriacaoDesc();

    List<PedidoAluguel> findByClienteIdOrderByDataCriacaoDesc(UUID clienteId);

    List<PedidoAluguel> findByStatusOrderByDataCriacaoDesc(StatusPedido status);

    boolean existsByAutomovelId(UUID automovelId);
}
