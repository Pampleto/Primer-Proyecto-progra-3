package repositorio;

import modelo.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de JDBC simulado: no ejecutan SQL en un servidor MySQL. */
class CotizacionRepositoryJdbcTest {
    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 10, 29);

    @TestFactory
    Stream<DynamicTest> filtrosIndividualesYCombinados() {
        return IntStream.range(0, 16).mapToObj(mascara -> DynamicTest.dynamicTest(
                "Filtros opcionales, combinacion " + mascara, () -> {
                    var f = new FiltroCotizaciones((mascara & 1) == 0 ? null : 7,
                            (mascara & 2) == 0 ? null : TipoDestino.INTERNACIONAL,
                            (mascara & 4) == 0 ? null : DESDE,
                            (mascara & 8) == 0 ? null : HASTA);
                    JdbcSimulado jdbc = new JdbcSimulado();
                    assertTrue(jdbc.repositorio.buscar(f).isEmpty());
                    String where = "WHERE 1 = 1";
                    Map<Integer, Object> esperados = new LinkedHashMap<>();
                    int indice = 1;
                    if (f.clienteId() != null) {
                        where += " AND c.cliente_id = ?";
                        esperados.put(indice++, 7);
                    }
                    if (f.destino() != null) {
                        where += " AND c.tipo_destino = ?";
                        esperados.put(indice++, "INTERNACIONAL");
                    }
                    if (f.fechaDesde() != null) {
                        where += " AND c.fecha_contratacion >= ?";
                        esperados.put(indice++, Timestamp.valueOf(DESDE.atStartOfDay()));
                    }
                    if (f.fechaHasta() != null) {
                        where += " AND c.fecha_contratacion <= ?";
                        esperados.put(indice, Timestamp.valueOf(HASTA.atTime(23, 59, 59)));
                    }
                    assertEquals(where + " ORDER BY c.id", jdbc.sql.substring(jdbc.sql.indexOf("WHERE")));
                    assertEquals(esperados, jdbc.parametros);
                    jdbc.verificarCierre();
                }));
    }

    @Test
    void nullYListarUsanLaMismaConsultaSinFiltros() {
        JdbcSimulado jdbc = new JdbcSimulado();
        assertTrue(jdbc.repositorio.buscar(null).isEmpty());
        String consulta = jdbc.sql;
        jdbc.repositorio.listar();
        assertEquals(consulta, jdbc.sql);
        assertTrue(jdbc.parametros.isEmpty());
        jdbc.verificarCierre();
    }

    @Test
    void reconstruyeLosTresPlanesConTarifaCatalogoYPrecioHistorico() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.filas = List.of(fila(1), fila(2), fila(3));
        var lista = jdbc.repositorio.buscar(FiltroCotizaciones.todos());
        assertEquals(3, lista.size());
        Class<?>[] tipos = {PlanBasico.class, PlanCoberturaTotal.class, PlanDeportesExtremos.class};
        for (int i = 0; i < 3; i++) {
            var c = lista.get(i);
            assertEquals(15, c.getId());
            assertEquals(7, c.getCliente().getId());
            assertEquals("Ana", c.getCliente().getNombre());
            assertEquals(25, c.getCliente().getEdad());
            assertEquals(TipoDestino.INTERNACIONAL, c.getViaje().getDestino());
            assertEquals(tipos[i], c.getPlan().getClass());
            assertEquals(4500.25, c.getPlan().getTarifaDiaria());
            assertEquals(27001.5, c.getPlan().calcularPrecio(c.getViaje()));
            assertEquals(150.25, c.getPrecioFinal());
        }
        jdbc.verificarCierre();
    }

    @Test
    void fechasInclusivasPermitenMismoDiaYLimitesDatetime() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.repositorio.buscar(new FiltroCotizaciones(null, null, DESDE, DESDE));
        assertEquals(Timestamp.valueOf(DESDE.atStartOfDay()), jdbc.parametros.get(1));
        assertEquals(Timestamp.valueOf(DESDE.atTime(23, 59, 59)), jdbc.parametros.get(2));
        assertDoesNotThrow(() -> new FiltroCotizaciones(null, null,
                LocalDate.of(1000, 1, 1), LocalDate.of(9999, 12, 31)));
        jdbc.verificarCierre();
    }

    @Test
    void rechazaCriteriosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new FiltroCotizaciones(0, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new FiltroCotizaciones(-1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new FiltroCotizaciones(null, null, HASTA, DESDE));
        assertThrows(IllegalArgumentException.class, () -> new FiltroCotizaciones(null, null,
                LocalDate.of(999, 12, 31), null));
        assertThrows(IllegalArgumentException.class, () -> new FiltroCotizaciones(null, null,
                null, LocalDate.of(10000, 1, 1)));
    }

    @Test
    void estadisticasUsanCountSumHistoricoYLeftJoin() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.filas = List.of(resumen(1, 1, 2, "0.30"), resumen(2, 1, 2, "0.30"), resumen(3, 0, 2, "0.30"));
        var resultado = jdbc.repositorio.obtenerResumen();
        assertEquals(2, resultado.totalCotizaciones());
        assertEquals(new BigDecimal("0.30"), resultado.sumaPreciosFinales());
        assertEquals(Map.of(1, 1L, 2, 1L, 3, 0L), resultado.cantidadesPorPlan());
        assertTrue(jdbc.sql.contains("COUNT(*) FROM cotizaciones"));
        assertTrue(jdbc.sql.contains("COALESCE(SUM(precio_final), 0)"));
        assertTrue(jdbc.sql.contains("COUNT(c.id)"));
        assertTrue(jdbc.sql.contains("LEFT JOIN cotizaciones c ON c.plan_id = p.id"));
        assertTrue(jdbc.sql.contains("GROUP BY p.id"));
        assertFalse(jdbc.sql.contains("tarifa_diaria"));
        assertThrows(UnsupportedOperationException.class, () -> resultado.cantidadesPorPlan().put(1, 9L));
        jdbc.verificarCierre();
    }

    @Test
    void estadisticasSinCotizacionesConservanPlanesEnCero() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.filas = List.of(resumen(1, 0, 0, "0.00"), resumen(2, 0, 0, "0.00"), resumen(3, 0, 0, "0.00"));
        var resultado = jdbc.repositorio.obtenerResumen();
        assertEquals(0, resultado.totalCotizaciones());
        assertEquals(new BigDecimal("0.00"), resultado.sumaPreciosFinales());
        assertEquals(Map.of(1, 0L, 2, 0L, 3, 0L), resultado.cantidadesPorPlan());
        jdbc.verificarCierre();
    }

    @Test
    void estadisticasConCatalogoVacioDevuelvenResumenVacio() {
        JdbcSimulado jdbc = new JdbcSimulado();
        var resultado = jdbc.repositorio.obtenerResumen();
        assertEquals(0, resultado.totalCotizaciones());
        assertEquals(new BigDecimal("0.00"), resultado.sumaPreciosFinales());
        assertTrue(resultado.cantidadesPorPlan().isEmpty());
        jdbc.verificarCierre();
    }

    @Test
    void estadisticasNoTruncanCantidadesGrandesNiMontos() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.filas = List.of(resumen(1, 3000000000L, 3000000000L, "12345678901234567890.99"));
        var resultado = jdbc.repositorio.obtenerResumen();
        assertEquals(3000000000L, resultado.totalCotizaciones());
        assertEquals(3000000000L, resultado.cantidadesPorPlan().get(1));
        assertEquals(new BigDecimal("12345678901234567890.99"), resultado.sumaPreciosFinales());
        jdbc.verificarCierre();
    }

    @Test
    void erroresSqlEnBusquedaYResumenConservanCausaYCierranRecursos() {
        for (boolean resumen : new boolean[]{false, true}) {
            JdbcSimulado jdbc = new JdbcSimulado();
            jdbc.fallo = new SQLException("Fallo simulado");
            var error = assertThrows(PersistenciaException.class, () -> {
                if (resumen) jdbc.repositorio.obtenerResumen();
                else jdbc.repositorio.buscar(FiltroCotizaciones.todos());
            });
            assertSame(jdbc.fallo, error.getCause());
            jdbc.verificarCierre();
        }
    }

    @Test
    void errorDeMapeoTambienCierraResultSet() {
        JdbcSimulado jdbc = new JdbcSimulado();
        jdbc.filas = List.of(fila(99));
        var error = assertThrows(PersistenciaException.class, () -> jdbc.repositorio.buscar(null));
        assertInstanceOf(SQLException.class, error.getCause());
        jdbc.verificarCierre();
    }

    @Test
    void crudAnteriorMantieneIdsYParametros() {
        JdbcSimulado jdbc = new JdbcSimulado();
        var nueva = new Cotizacion(new Cliente(7, "Ana", 25),
                new Viaje(TipoDestino.INTERNACIONAL, 3), new PlanBasico(), 150.25);
        jdbc.claves = List.of(Map.of(1, 15));
        var guardada = jdbc.repositorio.guardar(nueva);
        assertEquals(15, guardada.getId());
        assertTrue(nueva.getId() < 0);
        assertEquals(Statement.RETURN_GENERATED_KEYS, jdbc.modoClaves);
        assertEquals(Map.of(1, 7, 2, 1, 3, "INTERNACIONAL", 4, 3,
                5, new BigDecimal("150.25")), jdbc.parametros);
        jdbc.filas = List.of(fila(1));
        assertEquals(15, jdbc.repositorio.buscarPorId(15).orElseThrow().getId());
        assertEquals(Map.of(1, 15), jdbc.parametros);
        assertTrue(jdbc.repositorio.actualizar(guardada));
        assertEquals(15, jdbc.parametros.get(6));
        assertTrue(jdbc.sql.endsWith("WHERE id = ?"));
        jdbc.filas = List.of(Map.of(1, 1));
        assertEquals(1, jdbc.repositorio.contar());
        assertTrue(jdbc.repositorio.eliminar(15));
        assertEquals("DELETE FROM cotizaciones WHERE id = ?", jdbc.sql);
        assertEquals(Map.of(1, 15), jdbc.parametros);
        jdbc.filas = List.of();
        assertTrue(jdbc.repositorio.buscarPorId(15).isEmpty());
        jdbc.verificarCierre();
    }

    private static Map<Object, Object> fila(int planId) {
        Map<Object, Object> fila = new HashMap<>();
        fila.putAll(Map.of("id", 15, "cliente_id", 7, "cliente_nombre", "Ana", "cliente_edad", 25,
                "plan_id", planId, "plan_nombre", "Plan catalogo", "plan_descripcion", "Descripcion",
                "tarifa_diaria", new BigDecimal("4500.25"), "tipo_destino", "INTERNACIONAL", "cantidad_dias", 3));
        fila.put("precio_final", new BigDecimal("150.25"));
        return fila;
    }

    private static Map<Object, Object> resumen(int plan, long cantidad, long total, String monto) {
        return Map.of("plan_id", plan, "cantidad", cantidad, "total", total, "suma_total", new BigDecimal(monto));
    }

    /** Simula el protocolo JDBC y captura SQL/parametros; no evalua SQL. */
    private static class JdbcSimulado {
        String sql;
        Map<Integer, Object> parametros = new HashMap<>();
        List<Map<Object, Object>> filas = List.of();
        List<Map<Object, Object>> claves = List.of();
        SQLException fallo;
        int abiertos;
        int cerrados;
        int modoClaves;
        final CotizacionRepositoryJdbc repositorio = new CotizacionRepositoryJdbc(this::abrir);

        @SuppressWarnings("unchecked")
        private <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler manejador) {
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{tipo}, manejador);
        }

        Connection abrir() {
            abiertos++;
            return proxy(Connection.class, (p, m, args) -> {
                if (m.getName().equals("close")) { cerrados++; return null; }
                if (m.getName().equals("prepareStatement")) {
                    sql = ((String) args[0]).replaceAll("\\s+", " ").trim();
                    parametros = new HashMap<>();
                    modoClaves = args.length == 2 ? (int) args[1] : 0;
                    return sentencia();
                }
                throw new AssertionError("Conexion inesperada: " + m.getName());
            });
        }

        PreparedStatement sentencia() {
            abiertos++;
            return proxy(PreparedStatement.class, (p, m, args) -> {
                String nombre = m.getName();
                if (nombre.equals("close")) { cerrados++; return null; }
                if (nombre.startsWith("set")) { parametros.put((int) args[0], args[1]); return null; }
                if (nombre.startsWith("execute") && fallo != null) throw fallo;
                if (nombre.equals("executeQuery")) return resultado(filas);
                if (nombre.equals("executeUpdate")) return 1;
                if (nombre.equals("getGeneratedKeys")) return resultado(claves);
                throw new AssertionError("Sentencia inesperada: " + nombre);
            });
        }

        ResultSet resultado(List<Map<Object, Object>> datos) {
            abiertos++;
            int[] posicion = {-1};
            return proxy(ResultSet.class, (p, m, args) -> {
                if (m.getName().equals("close")) { cerrados++; return null; }
                if (m.getName().equals("next")) return ++posicion[0] < datos.size();
                Object valor = datos.get(posicion[0]).get(args[0]);
                return switch (m.getName()) {
                    case "getInt" -> ((Number) valor).intValue();
                    case "getLong" -> ((Number) valor).longValue();
                    case "getString" -> (String) valor;
                    case "getBigDecimal" -> (BigDecimal) valor;
                    default -> throw new AssertionError("ResultSet inesperado: " + m.getName());
                };
            });
        }

        void verificarCierre() {
            assertTrue(abiertos > 0);
            assertEquals(abiertos, cerrados, "Todos los recursos JDBC deben cerrarse");
        }
    }
}
