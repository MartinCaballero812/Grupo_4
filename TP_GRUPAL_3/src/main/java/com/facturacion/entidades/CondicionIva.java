package com.facturacion.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "condicion_iva")
public class CondicionIva extends AuditoriaApp {

    @Column(name = "codigo_afip", nullable = false)
    private int codigoAfip;

    @Column(nullable = false)
    private String denominacion;

    public CondicionIva() {
    }

    public CondicionIva(int codigoAfip, String denominacion) {
        this.codigoAfip = codigoAfip;
        this.denominacion = denominacion;
    }

    public int getCodigoAfip() {
        return codigoAfip;
    }

    public void setCodigoAfip(int codigoAfip) {
        this.codigoAfip = codigoAfip;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }
}
