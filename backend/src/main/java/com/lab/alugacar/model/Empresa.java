package com.lab.alugacar.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresas")
@DiscriminatorValue("EMPRESA")
public class Empresa extends Agente {

    public Empresa() {
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.EMPRESA;
    }
}
