package com.facturacion;

import java.util.Arrays;
import java.util.List;

import com.facturacion.entidades.Articulo;
import com.facturacion.entidades.Cliente;
import com.facturacion.entidades.FacturaVenta;
import com.facturacion.entidades.FacturaVentaDetalle;
import com.facturacion.entidades.Marca;
import com.facturacion.entidades.PuntoVenta;
import com.facturacion.entidades.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {

    public static void main(String[] args) {
        // --recargar: vacía todas las tablas y vuelve a cargar los datos de prueba
        boolean recargar = Arrays.asList(args).contains("--recargar");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            CargaDatos cargaDatos = new CargaDatos(em);

            em.getTransaction().begin();
            if (recargar) {
                cargaDatos.vaciarTablas();
            }
            boolean cargar = !cargaDatos.hayDatos();
            if (cargar) {
                cargaDatos.cargar();
            }
            em.getTransaction().commit();

            System.out.println("==============================================");
            System.out.println(cargar
                    ? "Datos de prueba cargados"
                    : "La base ya tenía datos: se omite la carga (usar --recargar para regenerarlos)");
            mostrarResumen(em);

            new ConsultasJPQL(em).ejecutarTodas();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    /** Lee desde la base la cantidad de registros de cada entidad (los detalles se insertaron en cascada). */
    private static void mostrarResumen(EntityManager em) {
        em.clear();
        for (Class<?> entidad : List.of(Usuario.class, PuntoVenta.class, Cliente.class, Marca.class,
                Articulo.class, FacturaVenta.class, FacturaVentaDetalle.class)) {
            Long cantidad = em.createQuery("SELECT COUNT(e) FROM " + entidad.getSimpleName() + " e", Long.class)
                    .getSingleResult();
            System.out.printf("%-20s %d%n", entidad.getSimpleName(), cantidad);
        }
        System.out.println("==============================================");
    }
}
