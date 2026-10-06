package com.facturacion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.facturacion.entidades.Articulo;
import com.facturacion.entidades.Cliente;
import com.facturacion.entidades.FacturaVenta;
import com.facturacion.entidades.FacturaVentaDetalle;
import com.facturacion.entidades.Marca;
import com.facturacion.entidades.PuntoVenta;

import jakarta.persistence.EntityManager;

/**
 * Consultas del TP Grupal JPQL. Cada método resuelve una consigna con
 * {@code em.createQuery(jpql, Clase.class)} y parámetros nombrados; {@link #ejecutarTodas()}
 * las corre con valores de ejemplo (tomados de {@link CargaDatos}) e imprime los resultados.
 */
public class ConsultasJPQL {

    private final EntityManager em;

    public ConsultasJPQL(EntityManager em) {
        this.em = em;
    }

    // =====================================================================
    // Nivel 1: Consultas básicas y proyecciones
    // =====================================================================

    /** 1. Todas las facturas de venta. */
    public List<FacturaVenta> facturas() {
        return em.createQuery("SELECT f FROM FacturaVenta f", FacturaVenta.class)
                .getResultList();
    }

    /** 2. Número, fecha de emisión e importe total de todas las facturas. */
    public List<Object[]> numeroFechaEImporteDeFacturas() {
        return em.createQuery(
                        "SELECT f.numero, f.fechaEmision, f.importeTotal FROM FacturaVenta f", Object[].class)
                .getResultList();
    }

    /** 3. Artículos de un rubro con una denominación determinada. */
    public List<Articulo> articulosPorRubro(String denominacionRubro) {
        return em.createQuery(
                        "SELECT a FROM Articulo a WHERE a.rubro.denominacion = :rubro", Articulo.class)
                .setParameter("rubro", denominacionRubro)
                .getResultList();
    }

    /** 4. Facturas emitidas dentro de un rango de fechas (ambos extremos incluidos). */
    public List<FacturaVenta> facturasEntreFechas(Date desde, Date hasta) {
        return em.createQuery("""
                        SELECT f FROM FacturaVenta f
                        WHERE f.fechaEmision BETWEEN :desde AND :hasta
                        ORDER BY f.fechaEmision""", FacturaVenta.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }

    // =====================================================================
    // Nivel 2: Condicionales combinados, operadores de texto y agregaciones
    // =====================================================================

    /** 5. Facturas en un estado, con importe total mayor a un mínimo y sin anular. */
    public List<FacturaVenta> facturasVigentesPorEstadoEImporte(String estado, double importeMinimo) {
        return em.createQuery("""
                        SELECT f FROM FacturaVenta f
                        WHERE f.estado = :estado
                          AND f.importeTotal > :importeMinimo
                          AND f.fechaAnulacion IS NULL""", FacturaVenta.class)
                .setParameter("estado", estado)
                .setParameter("importeMinimo", importeMinimo)
                .getResultList();
    }

    /** 6. Clientes cuya denominación contiene un texto (sin distinguir mayúsculas) o cuyo CUIT/CUIL empieza con un prefijo. */
    public List<Cliente> clientesPorDenominacionOCuit(String textoDenominacion, String prefijoCuit) {
        return em.createQuery("""
                        SELECT c FROM Cliente c
                        WHERE LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :texto, '%'))
                           OR c.cuitCuil LIKE CONCAT(:prefijo, '%')""", Cliente.class)
                .setParameter("texto", textoDenominacion)
                .setParameter("prefijo", prefijoCuit)
                .getResultList();
    }

    /** 7. Estados registrados en las facturas, sin duplicados y en orden alfabético. */
    public List<String> estadosDeFacturas() {
        return em.createQuery(
                        "SELECT DISTINCT f.estado FROM FacturaVenta f ORDER BY f.estado ASC", String.class)
                .getResultList();
    }

    /** 8. Cantidad, suma y promedio del importe total de las facturas en un estado, en un solo arreglo. */
    public Object[] totalesDeFacturas(String estado) {
        return em.createQuery("""
                        SELECT COUNT(f), SUM(f.importeTotal), AVG(f.importeTotal)
                        FROM FacturaVenta f
                        WHERE f.estado = :estado""", Object[].class)
                .setParameter("estado", estado)
                .getSingleResult();
    }

    /** 9. Puntos de venta cuyo número está en la lista recibida. */
    public List<PuntoVenta> puntosDeVentaPorNumero(List<Integer> numeros) {
        return em.createQuery(
                        "SELECT p FROM PuntoVenta p WHERE p.numero IN :numeros ORDER BY p.numero", PuntoVenta.class)
                .setParameter("numeros", numeros)
                .getResultList();
    }

    // =====================================================================
    // Nivel 3: Navegación de entidades, JOINs y subconsultas simples
    // =====================================================================

    /** 10. Facturas cargadas por un usuario, navegando usuarioCarga.usuario (join implícito). */
    public List<FacturaVenta> facturasPorUsuarioCarga(String usuario) {
        return em.createQuery(
                        "SELECT f FROM FacturaVenta f WHERE f.usuarioCarga.usuario = :usuario", FacturaVenta.class)
                .setParameter("usuario", usuario)
                .getResultList();
    }

    /** 11. Detalles de las facturas emitidas por un punto de venta (INNER JOIN explícito). */
    public List<FacturaVentaDetalle> detallesPorPuntoVenta(int numeroPuntoVenta) {
        return em.createQuery("""
                        SELECT d FROM FacturaVentaDetalle d
                        INNER JOIN d.factura f
                        INNER JOIN f.puntoVenta p
                        WHERE p.numero = :numeroPuntoVenta
                        ORDER BY f.numero""", FacturaVentaDetalle.class)
                .setParameter("numeroPuntoVenta", numeroPuntoVenta)
                .getResultList();
    }

    /** 12. Denominación de todos los artículos y de su marca, incluidos los que no tienen marca (LEFT JOIN). */
    public List<Object[]> articulosConMarca() {
        return em.createQuery("""
                        SELECT a.denominacion, m.denominacion
                        FROM Articulo a
                        LEFT JOIN a.marca m
                        ORDER BY a.denominacion""", Object[].class)
                .getResultList();
    }

    /** 13. Facturas con al menos un detalle de un artículo de la marca indicada. */
    public List<FacturaVenta> facturasConArticulosDeMarca(String denominacionMarca) {
        return em.createQuery("""
                        SELECT DISTINCT f FROM FacturaVenta f
                        JOIN f.detalles d
                        JOIN d.listaPrecioArticulo lpa
                        JOIN lpa.articulo a
                        JOIN a.marca m
                        WHERE m.denominacion = :marca""", FacturaVenta.class)
                .setParameter("marca", denominacionMarca)
                .getResultList();
    }

    /** 14. Facturas cuyo importe total supera el promedio de todas las facturas. */
    public List<FacturaVenta> facturasSobreElPromedio() {
        return em.createQuery("""
                        SELECT f FROM FacturaVenta f
                        WHERE f.importeTotal > (SELECT AVG(f2.importeTotal) FROM FacturaVenta f2)
                        ORDER BY f.importeTotal DESC""", FacturaVenta.class)
                .getResultList();
    }

    // =====================================================================
    // Nivel 4: Agrupamiento (GROUP BY) y filtros de grupo (HAVING)
    // =====================================================================

    /** 15. Por punto de venta: descripción, cantidad de facturas y total facturado. */
    public List<Object[]> facturacionPorPuntoVenta() {
        return em.createQuery("""
                        SELECT p.descripcion, COUNT(f), SUM(f.importeTotal)
                        FROM FacturaVenta f
                        JOIN f.puntoVenta p
                        GROUP BY p.numero, p.descripcion
                        ORDER BY p.numero""", Object[].class)
                .getResultList();
    }

    /** 16. Usuarios de carga que registraron más facturas que el mínimo indicado. */
    public List<Object[]> usuariosConMasFacturasQue(long cantidadMinima) {
        return em.createQuery("""
                        SELECT u.usuario, u.nombre, u.apellido, COUNT(f)
                        FROM FacturaVenta f
                        JOIN f.usuarioCarga u
                        GROUP BY u.id, u.usuario, u.nombre, u.apellido
                        HAVING COUNT(f) > :cantidadMinima
                        ORDER BY COUNT(f) DESC""", Object[].class)
                .setParameter("cantidadMinima", cantidadMinima)
                .getResultList();
    }

    /** 17. Por marca: unidades vendidas y subtotal acumulado de los detalles facturados. */
    public List<Object[]> ventasPorMarca() {
        return em.createQuery("""
                        SELECT m.denominacion, SUM(d.cantidad), SUM(d.importeSubtotal)
                        FROM FacturaVentaDetalle d
                        JOIN d.listaPrecioArticulo lpa
                        JOIN lpa.articulo a
                        JOIN a.marca m
                        GROUP BY m.id, m.denominacion
                        ORDER BY SUM(d.importeSubtotal) DESC""", Object[].class)
                .getResultList();
    }

    // =====================================================================
    // Nivel 5: Subconsultas correlacionadas, EXISTS, NOT EXISTS y CASE WHEN
    // =====================================================================

    /** 18. Marcas con al menos un artículo facturado (subconsulta correlacionada con EXISTS). */
    public List<Marca> marcasFacturadas() {
        return em.createQuery("""
                        SELECT m FROM Marca m
                        WHERE EXISTS (
                            SELECT d FROM FacturaVentaDetalle d
                            WHERE d.listaPrecioArticulo.articulo.marca = m)
                        ORDER BY m.denominacion""", Marca.class)
                .getResultList();
    }

    /** 19. Artículos que nunca se incluyeron en un detalle de factura (NOT EXISTS). */
    public List<Articulo> articulosNuncaFacturados() {
        return em.createQuery("""
                        SELECT a FROM Articulo a
                        WHERE NOT EXISTS (
                            SELECT d FROM FacturaVentaDetalle d
                            WHERE d.listaPrecioArticulo.articulo = a)
                        ORDER BY a.codigo""", Articulo.class)
                .getResultList();
    }

    /** 20. Número, importe total y categoría de cada factura, de mayor a menor importe (CASE WHEN). */
    public List<Object[]> facturasPorCategoria(double limiteAltoValor, double limiteBajoValor) {
        return em.createQuery("""
                        SELECT f.numero, f.importeTotal,
                               CASE WHEN f.importeTotal > :limiteAlto THEN 'ALTO VALOR'
                                    WHEN f.importeTotal >= :limiteBajo THEN 'MEDIO VALOR'
                                    ELSE 'BAJO VALOR'
                               END AS categoria
                        FROM FacturaVenta f
                        ORDER BY f.importeTotal DESC""", Object[].class)
                .setParameter("limiteAlto", limiteAltoValor)
                .setParameter("limiteBajo", limiteBajoValor)
                .getResultList();
    }

    // =====================================================================
    // Ejecución de ejemplo
    // =====================================================================

    public void ejecutarTodas() {
        titulo(1, "Todas las facturas de venta");
        facturas().forEach(f -> System.out.println(factura(f)));

        titulo(2, "Número, fecha de emisión e importe total");
        numeroFechaEImporteDeFacturas().forEach(r -> System.out.printf("N° %-3s %tF  %s%n", r[0], r[1], pesos(r[2])));

        titulo(3, "Artículos del rubro \"Electrónica\"");
        articulosPorRubro("Electrónica").forEach(a -> System.out.println(articulo(a)));

        titulo(4, "Facturas emitidas entre el 01/03/2026 y el 31/05/2026");
        facturasEntreFechas(fecha("2026-03-01"), fecha("2026-05-31")).forEach(f -> System.out.println(factura(f)));

        titulo(5, "Facturas EMITIDA, importe > $10.000 y sin anular");
        facturasVigentesPorEstadoEImporte("EMITIDA", 10000).forEach(f -> System.out.println(factura(f)));

        titulo(6, "Clientes cuya denominación contiene \"ANDES\" o cuyo CUIT/CUIL empieza con \"20-\"");
        clientesPorDenominacionOCuit("ANDES", "20-")
                .forEach(c -> System.out.printf("%s  %s%n", c.getCuitCuil(), c.getDenominacion()));

        titulo(7, "Estados de factura (sin duplicados, A-Z)");
        estadosDeFacturas().forEach(System.out::println);

        titulo(8, "Cantidad, suma y promedio de las facturas EMITIDA");
        Object[] totales = totalesDeFacturas("EMITIDA");
        System.out.printf("Cantidad: %s  Suma: %s  Promedio: %s%n", totales[0], pesos(totales[1]), pesos(totales[2]));

        titulo(9, "Puntos de venta con número 1, 2 o 5");
        puntosDeVentaPorNumero(List.of(1, 2, 5))
                .forEach(p -> System.out.printf("%04d  %s%n", p.getNumero(), p.getDescripcion()));

        titulo(10, "Facturas cargadas por el usuario \"lmartinez\"");
        facturasPorUsuarioCarga("lmartinez").forEach(f -> System.out.println(factura(f)));

        titulo(11, "Detalles de las facturas del punto de venta 5");
        detallesPorPuntoVenta(5).forEach(d -> System.out.printf("Factura %s  %-28s x %-4.0f %s%n",
                d.getFactura().getNumero(), d.getDescripcion(), d.getCantidad(), pesos(d.getImporteSubtotal())));

        titulo(12, "Artículos y su marca (incluye artículos sin marca)");
        articulosConMarca().forEach(r -> System.out.printf("%-30s %s%n", r[0], r[1] != null ? r[1] : "(sin marca)"));

        titulo(13, "Facturas con artículos de la marca \"Sony\"");
        facturasConArticulosDeMarca("Sony").forEach(f -> System.out.println(factura(f)));

        titulo(14, "Facturas con importe total mayor al promedio");
        facturasSobreElPromedio().forEach(f -> System.out.println(factura(f)));

        titulo(15, "Facturación por punto de venta");
        facturacionPorPuntoVenta().forEach(r -> System.out.printf("%-22s %3s facturas  %s%n", r[0], r[1], pesos(r[2])));

        titulo(16, "Usuarios de carga con más de 5 facturas");
        usuariosConMasFacturasQue(5).forEach(r -> System.out.printf("%-12s %s %s  (%s facturas)%n", r[0], r[1], r[2], r[3]));

        titulo(17, "Unidades vendidas y subtotal acumulado por marca");
        ventasPorMarca().forEach(r -> System.out.printf("%-10s %5.0f unidades  %s%n", r[0], r[1], pesos(r[2])));

        titulo(18, "Marcas con al menos un artículo facturado (EXISTS)");
        marcasFacturadas().forEach(m -> System.out.println(m.getDenominacion()));

        titulo(19, "Artículos nunca facturados (NOT EXISTS)");
        articulosNuncaFacturados().forEach(a -> System.out.println(articulo(a)));

        titulo(20, "Categoría de cada factura (CASE WHEN), de mayor a menor importe");
        facturasPorCategoria(50000, 10000).forEach(r -> System.out.printf("N° %-3s %s  %s%n", r[0], pesos(r[1]), r[2]));
    }

    // ==================== Helpers de impresión ====================

    private static void titulo(int numero, String descripcion) {
        System.out.println();
        System.out.printf("########## CONSULTA %d: %s%n", numero, descripcion);
    }

    private static String factura(FacturaVenta f) {
        return String.format("%04d-%08d  %tF  %-9s  %-25s  %s",
                f.getPuntoVenta().getNumero(), f.getNumero(), f.getFechaEmision(), f.getEstado(),
                f.getCliente() != null ? f.getCliente().getDenominacion() : "(sin cliente)",
                pesos(f.getImporteTotal()));
    }

    private static String articulo(Articulo a) {
        return String.format("%s  %-30s %-12s %s", a.getCodigo(), a.getDenominacion(),
                a.getRubro().getDenominacion(), a.getMarca() != null ? a.getMarca().getDenominacion() : "(sin marca)");
    }

    private static String pesos(Object importe) {
        return String.format("$ %,14.2f", ((Number) importe).doubleValue());
    }

    private static Date fecha(String isoFecha) {
        return Date.from(LocalDate.parse(isoFecha).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
