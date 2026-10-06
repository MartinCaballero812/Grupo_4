# TP GRUPAL JPQL

Enunciados (raíz del proyecto):
- `TP GRUPAL JPQL.pdf` – 20 consultas JPQL

## Stack
- Java 17+ (compila con `release 17`; probado en JDK 25)
- Maven (no está en el PATH: `~/tools/apache-maven-3.9.11/bin/mvn`)
- Hibernate ORM 6.6 (`jakarta.persistence`). No migrar a Hibernate 7: el enunciado exige las
  propiedades `javax.persistence.jdbc.*` en `persistence.xml`, que Hibernate 7 ya no lee.
- PostgreSQL 18 local, base `facturacion`, usuario `postgres`

## Estructura
- `src/main/java/com/facturacion/entidades/` – `EntityId` y `AuditoriaApp` (`@MappedSuperclass`) + 14 entidades
- `src/main/java/com/facturacion/CargaDatos.java` – juego de datos de prueba para las consultas JPQL
- `src/main/java/com/facturacion/ConsultasJPQL.java` – las 20 consultas del TP JPQL, un método por consigna
  (agrupados por nivel); `ejecutarTodas()` las corre con valores de ejemplo e imprime los resultados
- `src/main/java/com/facturacion/Main.java` – carga los datos si la base está vacía, muestra un resumen
  y ejecuta todas las consultas JPQL
- `src/main/resources/META-INF/persistence.xml` – unidad `FacturacionPU` (`hbm2ddl.auto = update`)

## Cómo se persiste
- `Main` solo carga si no hay facturas (`CargaDatos.hayDatos()`); si ya hay datos, no inserta nada,
  así que ejecutar varias veces **no duplica**. `--recargar` hace `TRUNCATE ... RESTART IDENTITY CASCADE`
  de todas las tablas y vuelve a cargar (ids desde 1).
- Los maestros (usuarios, puntos de venta, rubros, marcas, artículos, precios, clientes...) se persisten
  explícitamente, porque algunos no son alcanzables desde ninguna factura. `Contacto` y `Domicilio`
  entran por el `@OneToOne(cascade = ALL)` de `Cliente`.
- Cada factura se guarda con **un único** `em.persist(factura)`; sus `FacturaVentaDetalle` se insertan por
  `@OneToMany(cascade = ALL)`. Los `@ManyToOne` también tienen `cascade = PERSIST` (por eso persistir una
  factura alcanzaría para guardar sus maestros si no estuvieran ya persistidos).

## Datos de prueba (`CargaDatos`)
- 4 usuarios: `admin` (12 facturas), `lmartinez` (9), `cfernandez` (4), `auditor` (0)
- 5 puntos de venta (números 1, 2, 3, 5, 7; el 7 sin facturas), 4 condiciones de IVA, 2 monedas (solo se factura en PES)
- 4 rubros (Informática, Electrónica, Audio, Hogar), 7 marcas (Genius sin artículos facturados, Noblex sin artículos)
- 15 artículos con precio en Lista Minorista y Mayorista (85%): 3 sin marca, 3 nunca facturados
  (ART-013, ART-014, ART-015)
- 8 clientes (CUIT 20-, 23-, 27-, 30-), algunas facturas sin cliente (venta de mostrador)
- 25 facturas entre ene y sep 2026 con estados EMITIDA, ANULADA (con `fechaAnulacion`), RECHAZADA
  (`resultadoAfip = "R"`, `motivoRechazo`) y PENDIENTE; importes de ~$7.800 a ~$3.500.000

## Comandos
- Compilar: `mvn compile`
- Ejecutar: `mvn compile exec:java`
- Regenerar los datos desde cero: `mvn compile exec:java -Dexec.args="--recargar"`

## Convenciones
- Fechas con `java.util.Date` + `@Temporal` (respeta el modelo del enunciado)
- Tablas y columnas en snake_case (`@Table(name = "factura_venta")`, `@Column(name = "fecha_alta")`)
- La relación FacturaVenta ↔ FacturaVentaDetalle se arma siempre con `factura.addDetalle(...)`
- Los datos de auditoría se completan con `registrarAlta(usuario)`; los importes de la factura, con `calcularTotales()`
