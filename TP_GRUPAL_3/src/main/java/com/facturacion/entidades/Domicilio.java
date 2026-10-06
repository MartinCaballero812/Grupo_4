package com.facturacion.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "domicilio")
public class Domicilio extends EntityId {

    @Column(name = "nombre_calle")
    private String nombreCalle;

    @Column(name = "numero_calle")
    private String numeroCalle;

    public Domicilio() {
    }

    public Domicilio(String nombreCalle, String numeroCalle) {
        this.nombreCalle = nombreCalle;
        this.numeroCalle = numeroCalle;
    }

    public String getNombreCalle() {
        return nombreCalle;
    }

    public void setNombreCalle(String nombreCalle) {
        this.nombreCalle = nombreCalle;
    }

    public String getNumeroCalle() {
        return numeroCalle;
    }

    public void setNumeroCalle(String numeroCalle) {
        this.numeroCalle = numeroCalle;
    }
}
