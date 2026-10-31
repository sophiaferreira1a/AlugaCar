package com.lab.alugacar.repository;

import com.lab.alugacar.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomovelRepository extends JpaRepository<Automovel, UUID> {

    List<Automovel> findAllByOrderByMarcaAscModeloAsc();

    List<Automovel> findByProprietarioIdOrderByMarcaAscModeloAsc(UUID proprietarioId);

    boolean existsByMatricula(String matricula);

    boolean existsByPlaca(String placa);

    boolean existsByMatriculaAndIdNot(String matricula, UUID id);

    boolean existsByPlacaAndIdNot(String placa, UUID id);
}
