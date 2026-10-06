package com.facturacion.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "factura_venta_detalle")
public class FacturaVentaDetalle extends EntityId {

    @ManyToOne
    @JoinColumn(name = "factura_id", nullable = false)
    private FacturaVenta factura;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "lista_precio_articulo_id", nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;

    private String descripcion;

    @Column(nullable = false)
    private double cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private double precioUnitario;

    @Column(name = "porcentaje_bonificacion")
    private double porcentajeBonificacion;

    @Column(name = "importe_neto")
    private double importeNeto;

    @Column(name = "importe_iva")
    private double importeIva;

    @Column(name = "importe_subtotal", nullable = false)
    private double importeSubtotal;

    public FacturaVentaDetalle() {
    }

    /**
     * Crea un ítem tomando descripción y precio de la lista de precios y calcula sus importes.
     *
     * @param alicuotaIva alícuota de IVA en porcentaje (ej. 21.0)
     */
    public FacturaVentaDetalle(ListaPrecioArticulo listaPrecioArticulo, double cantidad,
                               double porcentajeBonificacion, double alicuotaIva) {
        this.listaPrecioArticulo = listaPrecioArticulo;
        this.descripcion = listaPrecioArticulo.getArticulo().getDenominacion();
        this.cantidad = cantidad;
        this.precioUnitario = listaPrecioArticulo.getPrecioVenta();
        this.porcentajeBonificacion = porcentajeBonificacion;
        this.importeNeto = cantidad * precioUnitario * (1 - porcentajeBonificacion / 100);
        this.importeIva = importeNeto * alicuotaIva / 100;
        this.importeSubtotal = importeNeto + importeIva;
    }

    public FacturaVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaVenta factura) {
        this.factura = factura;
    }

    public ListaPrecioArticulo getListaPrecioArticulo() {
        return listaPrecioArticulo;
    }

    public void setListaPrecioArticulo(ListaPrecioArticulo listaPrecioArticulo) {
        this.listaPrecioArticulo = listaPrecioArticulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getPorcentajeBonificacion() {
        return porcentajeBonificacion;
    }

    public void setPorcentajeBonificacion(double porcentajeBonificacion) {
        this.porcentajeBonificacion = porcentajeBonificacion;
    }

    public double getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(double importeNeto) {
        this.importeNeto = importeNeto;
    }

    public double getImporteIva() {
        return importeIva;
    }

    public void setImporteIva(double importeIva) {
        this.importeIva = importeIva;
    }

    public double getImporteSubtotal() {
        return importeSubtotal;
    }

    public void setImporteSubtotal(double importeSubtotal) {
        this.importeSubtotal = importeSubtotal;
    }
}
