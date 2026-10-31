package com.lab.alugacar.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "bancos")
@DiscriminatorValue("BANCO")
public class Banco extends Agente {

    @Column(name = "codigo_banco", nullable = false, unique = true, length = 10)
    private String codigoBanco;

    public Banco() {
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.BANCO;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public void setCodigoBanco(String codigoBanco) {
        this.codigoBanco = codigoBanco;
    }
}
