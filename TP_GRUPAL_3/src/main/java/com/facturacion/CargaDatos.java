package com.facturacion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facturacion.entidades.Articulo;
import com.facturacion.entidades.AuditoriaApp;
import com.facturacion.entidades.Cliente;
import com.facturacion.entidades.CondicionIva;
import com.facturacion.entidades.Contacto;
import com.facturacion.entidades.Domicilio;
import com.facturacion.entidades.FacturaVenta;
import com.facturacion.entidades.FacturaVentaDetalle;
import com.facturacion.entidades.ListaPrecio;
import com.facturacion.entidades.ListaPrecioArticulo;
import com.facturacion.entidades.Marca;
import com.facturacion.entidades.PuntoVenta;
import com.facturacion.entidades.Rubro;
import com.facturacion.entidades.TipoMoneda;
import com.facturacion.entidades.Usuario;

import jakarta.persistence.EntityManager;

/**
 * Juego de datos de prueba pensado para que las 20 consultas del TP JPQL devuelvan resultados
 * significativos: varios usuarios, puntos de venta, rubros y marcas, artículos sin marca,
 * artículos y marcas nunca facturados, y facturas en distintos estados, fechas e importes.
 * Todos los métodos deben llamarse dentro de una transacción activa.
 */
public class CargaDatos {

    private static final double ALICUOTA_IVA = 21.0;
    private static final double DESCUENTO_MAYORISTA = 0.85;

    /** Ítem a facturar: precio de lista, cantidad y porcentaje de bonificación. */
    private record Item(ListaPrecioArticulo precio, double cantidad, double bonificacion) {
    }

    private final EntityManager em;

    private final List<AuditoriaApp> maestros = new ArrayList<>();
    private final Map<Articulo, ListaPrecioArticulo> preciosMinorista = new HashMap<>();
    private final Map<Articulo, ListaPrecioArticulo> preciosMayorista = new HashMap<>();
    private TipoMoneda pesos;

    public CargaDatos(EntityManager em) {
        this.em = em;
    }

    public boolean hayDatos() {
        return em.createQuery("SELECT COUNT(f) FROM FacturaVenta f", Long.class).getSingleResult() > 0;
    }

    /** Vacía todas las tablas y reinicia los ids (secuencias IDENTITY) en 1. */
    public void vaciarTablas() {
        em.createNativeQuery("TRUNCATE TABLE factura_venta_detalle, factura_venta, lista_precio_articulo, "
                + "articulo, lista_precio, marca, rubro, cliente, contacto, domicilio, condicion_iva, "
                + "tipo_moneda, punto_venta, usuario RESTART IDENTITY CASCADE").executeUpdate();
    }

    public void cargar() {
        // ===== Usuarios =====
        Usuario admin = new Usuario("admin", "admin123", "Juan", "Pérez");
        Usuario lmartinez = new Usuario("lmartinez", "lm2026", "Laura", "Martínez");
        Usuario cfernandez = new Usuario("cfernandez", "cf2026", "Carlos", "Fernández");
        Usuario auditor = new Usuario("auditor", "audit01", "Sofía", "Romero"); // no registra facturas

        // ===== Puntos de venta, condiciones de IVA y monedas =====
        PuntoVenta casaCentral = maestro(new PuntoVenta(1, "Casa Central", "Electrónica", "Av. San Martín 1234, Mendoza"));
        PuntoVenta godoyCruz = maestro(new PuntoVenta(2, "Sucursal Godoy Cruz", "Electrónica", "Av. San Martín Sur 850, Godoy Cruz"));
        PuntoVenta maipu = maestro(new PuntoVenta(3, "Sucursal Maipú", "Controlador Fiscal", "Padre Vázquez 320, Maipú"));
        PuntoVenta online = maestro(new PuntoVenta(5, "Tienda Online", "Electrónica", "Depósito Las Heras - Venta web"));
        maestro(new PuntoVenta(7, "Sucursal Luján", "Electrónica", "Roque Sáenz Peña 1500, Luján de Cuyo")); // sin facturas

        CondicionIva responsableInscripto = maestro(new CondicionIva(1, "IVA Responsable Inscripto"));
        CondicionIva exento = maestro(new CondicionIva(4, "IVA Sujeto Exento"));
        CondicionIva consumidorFinal = maestro(new CondicionIva(5, "Consumidor Final"));
        CondicionIva monotributo = maestro(new CondicionIva(6, "Responsable Monotributo"));

        pesos = maestro(new TipoMoneda("PES", "Peso Argentino", "$"));
        maestro(new TipoMoneda("DOL", "Dólar Estadounidense", "US$"));

        // ===== Rubros, marcas y listas de precio =====
        Rubro informatica = maestro(new Rubro(1, "Informática"));
        Rubro electronica = maestro(new Rubro(2, "Electrónica"));
        Rubro audio = maestro(new Rubro(3, "Audio"));
        Rubro hogar = maestro(new Rubro(4, "Hogar"));

        Marca logitech = maestro(new Marca(1, "Logitech"));
        Marca hp = maestro(new Marca(2, "HP"));
        Marca samsung = maestro(new Marca(3, "Samsung"));
        Marca sony = maestro(new Marca(4, "Sony"));
        Marca philips = maestro(new Marca(5, "Philips"));
        Marca genius = maestro(new Marca(6, "Genius")); // tiene artículos, pero ninguno facturado
        maestro(new Marca(7, "Noblex"));                 // sin artículos

        ListaPrecio minorista = maestro(new ListaPrecio("LP01", "Lista Minorista"));
        ListaPrecio mayorista = maestro(new ListaPrecio("LP02", "Lista Mayorista"));

        // ===== Artículos (con su precio en ambas listas) =====
        Articulo mouse = articulo("ART-001", "Mouse inalámbrico M185", informatica, logitech, 15000, minorista, mayorista);
        Articulo teclado = articulo("ART-002", "Teclado K120", informatica, logitech, 22000, minorista, mayorista);
        Articulo webcam = articulo("ART-003", "Webcam C920 Full HD", informatica, logitech, 68000, minorista, mayorista);
        Articulo notebook = articulo("ART-004", "Notebook 15\" Ryzen 5", informatica, hp, 950000, minorista, mayorista);
        Articulo impresora = articulo("ART-005", "Impresora DeskJet 2775", informatica, hp, 180000, minorista, mayorista);
        Articulo pendrive = articulo("ART-006", "Pendrive 64GB", informatica, null, 9000, minorista, mayorista);
        Articulo televisor = articulo("ART-007", "Smart TV 50\" Crystal UHD", electronica, samsung, 780000, minorista, mayorista);
        Articulo celular = articulo("ART-008", "Celular Galaxy A15 128GB", electronica, samsung, 420000, minorista, mayorista);
        Articulo cableHdmi = articulo("ART-009", "Cable HDMI 2m", electronica, null, 6500, minorista, mayorista);
        Articulo auriculares = articulo("ART-010", "Auriculares WH-CH520", audio, sony, 85000, minorista, mayorista);
        Articulo parlante = articulo("ART-011", "Parlante portátil SRS-XB100", audio, sony, 95000, minorista, mayorista);
        Articulo pava = articulo("ART-012", "Pava eléctrica HD9350", hogar, philips, 65000, minorista, mayorista);
        // Nunca facturados
        articulo("ART-013", "Plancha a vapor GC1750", hogar, philips, 48000, minorista, mayorista);
        articulo("ART-014", "Mouse óptico DX-110", informatica, genius, 8500, minorista, mayorista);
        articulo("ART-015", "Zapatilla eléctrica 5 tomas", hogar, null, 12000, minorista, mayorista);

        // ===== Clientes =====
        Cliente gonzalez = cliente("20-12345678-9", "María González", "maria.gonzalez@mail.com", "261-4123456", "Belgrano", "550");
        Cliente ramirez = cliente("20-23456789-0", "Jorge Ramírez", "jramirez@mail.com", "261-4234567", "Colón", "1120");
        Cliente sosa = cliente("27-34567890-1", "Lucía Sosa", "lucia.sosa@mail.com", "261-4345678", "Sarmiento", "85");
        Cliente gomez = cliente("23-45678901-2", "Pablo Gómez", "pgomez@mail.com", "261-4456789", "Las Heras", "2301");
        Cliente torres = cliente("20-31234567-4", "Martín Torres", "mtorres@mail.com", "261-4567890", "Perú", "940");
        Cliente andes = cliente("30-71234567-8", "Distribuidora Andes S.A.", "compras@andes.com.ar", "261-4200100", "Acceso Este", "3500");
        Cliente tecnoCuyo = cliente("30-70987654-3", "Tecno Cuyo S.R.L.", "admin@tecnocuyo.com.ar", "261-4300200", "Godoy Cruz", "415");
        Cliente fundacion = cliente("30-69876543-2", "Fundación Educar Mendoza", "contacto@educar.org.ar", "261-4400300", "Rivadavia", "60");

        // Los maestros se persisten explícitamente: algunos (artículos o marcas nunca facturados,
        // el usuario auditor, la sucursal Luján) no son alcanzables desde ninguna factura.
        for (Usuario usuario : List.of(admin, lmartinez, cfernandez, auditor)) {
            em.persist(usuario);
        }
        for (AuditoriaApp maestro : maestros) {
            maestro.registrarAlta(admin);
            em.persist(maestro);
        }

        // ===== Facturas =====
        List<FacturaVenta> facturas = List.of(
                // Casa Central (PV 1)
                emitida(factura(1, "2026-01-08", casaCentral, gonzalez, consumidorFinal, admin,
                        min(mouse, 2), min(teclado, 1)), 1.0),
                emitida(factura(2, "2026-01-15", casaCentral, andes, responsableInscripto, admin,
                        may(notebook, 3, 5), may(impresora, 2, 5)), 0.5),
                emitida(factura(3, "2026-02-03", casaCentral, null, consumidorFinal, admin,
                        min(cableHdmi, 1)), 1.0),
                emitida(factura(4, "2026-02-20", casaCentral, tecnoCuyo, responsableInscripto, lmartinez,
                        may(televisor, 2, 0), may(cableHdmi, 2, 0)), 0.0),
                anulada(factura(5, "2026-03-11", casaCentral, ramirez, consumidorFinal, admin,
                        min(celular, 1)), "2026-03-12"),
                emitida(factura(6, "2026-04-02", casaCentral, torres, monotributo, admin,
                        min(auriculares, 1), min(pendrive, 2)), 1.0),
                emitida(factura(7, "2026-05-19", casaCentral, fundacion, exento, cfernandez,
                        min(webcam, 4), min(teclado, 4)), 0.3),
                emitida(factura(8, "2026-06-25", casaCentral, gonzalez, consumidorFinal, admin,
                        min(pava, 1)), 1.0),
                factura(9, "2026-09-28", casaCentral, sosa, consumidorFinal, lmartinez,
                        min(parlante, 1), min(auriculares, 1)),

                // Sucursal Godoy Cruz (PV 2)
                emitida(factura(1, "2026-01-22", godoyCruz, sosa, consumidorFinal, lmartinez,
                        min(televisor, 1)), 1.0),
                emitida(factura(2, "2026-02-14", godoyCruz, gomez, consumidorFinal, lmartinez,
                        min(mouse, 1)), 1.0),
                emitida(factura(3, "2026-03-05", godoyCruz, andes, responsableInscripto, admin,
                        may(celular, 5, 10)), 0.6),
                emitida(factura(4, "2026-04-17", godoyCruz, null, consumidorFinal, lmartinez,
                        min(cableHdmi, 1)), 1.0),
                rechazada(factura(5, "2026-05-08", godoyCruz, ramirez, consumidorFinal, cfernandez,
                        min(parlante, 1), min(cableHdmi, 1)), "Error en el importe de IVA informado"),
                emitida(factura(6, "2026-07-12", godoyCruz, gomez, consumidorFinal, lmartinez,
                        min(notebook, 1), min(mouse, 1)), 1.0),
                emitida(factura(7, "2026-08-30", godoyCruz, tecnoCuyo, responsableInscripto, admin,
                        may(auriculares, 10, 5), may(parlante, 6, 5)), 0.5),

                // Sucursal Maipú (PV 3)
                emitida(factura(1, "2026-02-27", maipu, torres, monotributo, cfernandez,
                        min(impresora, 1)), 1.0),
                emitida(factura(2, "2026-04-23", maipu, null, consumidorFinal, admin,
                        min(pava, 2)), 1.0),
                anulada(factura(3, "2026-06-03", maipu, gonzalez, consumidorFinal, lmartinez,
                        min(teclado, 1), min(webcam, 1)), "2026-06-04"),
                emitida(factura(4, "2026-08-14", maipu, sosa, consumidorFinal, cfernandez,
                        min(celular, 1), min(auriculares, 1)), 0.5),

                // Tienda Online (PV 5)
                emitida(factura(1, "2026-03-20", online, gomez, consumidorFinal, admin,
                        min(webcam, 1)), 1.0),
                emitida(factura(2, "2026-05-30", online, andes, responsableInscripto, lmartinez,
                        may(televisor, 4, 10), may(pava, 10, 10)), 0.0),
                rechazada(factura(3, "2026-07-21", online, ramirez, consumidorFinal, admin,
                        min(notebook, 1)), "CUIT/CUIL del receptor no registrado"),
                emitida(factura(4, "2026-09-15", online, fundacion, exento, lmartinez,
                        min(impresora, 2), min(pendrive, 5)), 1.0),
                factura(5, "2026-09-30", online, null, consumidorFinal, admin,
                        min(mouse, 1), min(cableHdmi, 1)));

        // Un único em.persist por factura: sus detalles se insertan por CascadeType.ALL
        for (FacturaVenta factura : facturas) {
            em.persist(factura);
        }
    }

    // ==================== Helpers de maestros ====================

    private <T extends AuditoriaApp> T maestro(T entidad) {
        maestros.add(entidad);
        return entidad;
    }

    private Articulo articulo(String codigo, String denominacion, Rubro rubro, Marca marca, double precioMinorista,
                              ListaPrecio minorista, ListaPrecio mayorista) {
        Articulo articulo = maestro(new Articulo(codigo, denominacion, rubro, marca));
        preciosMinorista.put(articulo, maestro(new ListaPrecioArticulo(minorista, articulo, precioMinorista)));
        preciosMayorista.put(articulo, maestro(new ListaPrecioArticulo(mayorista, articulo,
                Math.round(precioMinorista * DESCUENTO_MAYORISTA))));
        return articulo;
    }

    private Cliente cliente(String cuitCuil, String denominacion, String email, String telefono,
                            String calle, String numeroCalle) {
        Contacto contacto = new Contacto(email, telefono, null);
        return maestro(new Cliente(cuitCuil, denominacion, contacto, new Domicilio(calle, numeroCalle)));
    }

    // ==================== Helpers de facturas ====================

    private Item min(Articulo articulo, double cantidad) {
        return new Item(preciosMinorista.get(articulo), cantidad, 0);
    }

    private Item may(Articulo articulo, double cantidad, double bonificacion) {
        return new Item(preciosMayorista.get(articulo), cantidad, bonificacion);
    }

    /** Crea una factura en estado PENDIENTE (todavía sin autorizar por AFIP). */
    private FacturaVenta factura(long numero, String fechaEmision, PuntoVenta puntoVenta, Cliente cliente,
                                 CondicionIva condicionIva, Usuario usuario, Item... items) {
        FacturaVenta factura = new FacturaVenta();
        factura.registrarAlta(usuario);
        factura.setNumero(numero);
        factura.setFechaEmision(fecha(fechaEmision));
        factura.setPuntoVenta(puntoVenta);
        factura.setCliente(cliente);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(pesos);
        factura.setEstado("PENDIENTE");
        for (Item item : items) {
            factura.addDetalle(new FacturaVentaDetalle(item.precio(), item.cantidad(), item.bonificacion(), ALICUOTA_IVA));
        }
        factura.calcularTotales();
        return factura;
    }

    /** Autoriza la factura (CAE) y registra el cobro de un porcentaje (0 a 1) del total. */
    private FacturaVenta emitida(FacturaVenta factura, double porcentajeCobrado) {
        factura.setEstado("EMITIDA");
        factura.setResultadoAfip("A");
        factura.setCae(String.format("7412%04d%06d", factura.getPuntoVenta().getNumero(), factura.getNumero()));
        factura.setCaeFechaVencimiento(Date.from(factura.getFechaEmision().toInstant().plusSeconds(10L * 24 * 3600)));
        factura.setImporteCobrado(Math.round(factura.getImporteTotal() * porcentajeCobrado * 100) / 100.0);
        factura.calcularTotales();
        return factura;
    }

    private FacturaVenta anulada(FacturaVenta factura, String fechaAnulacion) {
        emitida(factura, 0);
        factura.setEstado("ANULADA");
        factura.setFechaAnulacion(fecha(fechaAnulacion));
        factura.setObservaciones("Anulada a pedido del cliente");
        return factura;
    }

    private FacturaVenta rechazada(FacturaVenta factura, String motivo) {
        factura.setEstado("RECHAZADA");
        factura.setResultadoAfip("R");
        factura.setMotivoRechazo(motivo);
        return factura;
    }

    private static Date fecha(String isoFecha) {
        return Date.from(LocalDate.parse(isoFecha).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
