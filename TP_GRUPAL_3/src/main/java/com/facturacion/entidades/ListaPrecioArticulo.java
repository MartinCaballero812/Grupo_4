package com.facturacion.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lista_precio_articulo")
public class ListaPrecioArticulo extends AuditoriaApp {

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "lista_precio_id", nullable = false)
    private ListaPrecio listaPrecio;

    @Column(name = "precio_venta", nullable = false)
    private double precioVenta;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    public ListaPrecioArticulo() {
    }

    public ListaPrecioArticulo(ListaPrecio listaPrecio, Articulo articulo, double precioVenta) {
        this.listaPrecio = listaPrecio;
        this.articulo = articulo;
        this.precioVenta = precioVenta;
    }

    public ListaPrecio getListaPrecio() {
        return listaPrecio;
    }

    public void setListaPrecio(ListaPrecio listaPrecio) {
        this.listaPrecio = listaPrecio;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }
}
