package servicio;

import modelo.Cliente;
import modelo.Cotizacion;
import modelo.PlanSeguro;
import modelo.TipoDestino;
import modelo.Viaje;
import org.junit.jupiter.api.Test;
import repositorio.CotizacionRepository;
import repositorio.PersistenciaException;

import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class CotizacionServicioTest {

    private final RepositorioPrueba repositorio = new RepositorioPrueba();
    private final CotizacionServicio servicio = new CotizacionServicio(repositorio);
    private final Cliente cliente = new Cliente(7, "Ana", 25);
    private final Viaje viaje = new Viaje(TipoDestino.NACIONAL, 3);
    private final CotizadorServicio cotizador = new CotizadorServicio();
    private final PlanSeguro plan = cotizador.obtenerPlanes().getFirst();

    @Test
    void constructorActualUsaJdbcYReportaConfiguracionAusente() {
        assumeFalse(Files.isRegularFile(Path.of("db.properties")));
        PersistenciaException error = assertThrows(PersistenciaException.class,
                () -> new CotizacionServicio().cantidadCotizaciones());
        assertInstanceOf(SQLException.class, error.getCause());
        assertTrue(error.getCause().getMessage().contains("No existe db.properties"));
    }

    @Test
    void conservaCalculosYNoPersisteDuranteLaCotizacion() {
        for (TipoDestino destino : TipoDestino.values()) {
            Map<String, Double> precios = cotizador.cotizar(new Viaje(destino, 3));
            double factor = destino == TipoDestino.NACIONAL ? 1 : 2;
            assertEquals(List.of(12000.0 * factor, 22500.0 * factor, 36000.0 * factor),
                    new ArrayList<>(precios.values()));
        }
        assertEquals(0, repositorio.inserciones);
    }

    @Test
    void confirmacionInsertaUnaVezYConservaIdYPrecioRecibidos() {
        double precio = cotizador.cotizar(viaje).get(plan.getNombre());
        Cotizacion guardada = servicio.registrarCotizacion(cliente, viaje, plan, precio);
        assertEquals(1, repositorio.inserciones);
        assertEquals(15, guardada.getId());
        assertSame(guardada, servicio.buscarCotizacionPorId(15));
        assertSame(cliente, guardada.getCliente());
        assertSame(viaje, guardada.getViaje());
        assertSame(plan, guardada.getPlan());
        assertEquals(precio, guardada.getPrecioFinal());
        assertTrue(repositorio.ultimaNueva.getId() < 0);
    }

    @Test
    void conservaPrecioConfirmadoSinRecalcular() {
        Cotizacion guardada = servicio.registrarCotizacion(cliente, viaje, plan, 150.25);
        assertEquals(150.25, guardada.getPrecioFinal());
    }

    @Test
    void cicloCompartidoDeConsultaModificacionConteoYEliminacion() {
        CotizacionServicio otraVista = new CotizacionServicio(repositorio);
        assertTrue(otraVista.estaVacio());
        Cotizacion guardada = servicio.registrarCotizacion(cliente, viaje, plan, 12000);
        assertEquals(List.of(guardada), otraVista.obtenerCotizaciones());
        Viaje nuevoViaje = new Viaje(TipoDestino.INTERNACIONAL, 4);
        assertTrue(servicio.modificarCotizacion(15, cliente, nuevoViaje, plan, 32000));
        Cotizacion actualizada = otraVista.buscarCotizacionPorId(15);
        assertEquals(15, actualizada.getId());
        assertEquals(32000, actualizada.getPrecioFinal());
        assertSame(nuevoViaje, actualizada.getViaje());
        assertEquals(1, otraVista.cantidadCotizaciones());
        assertFalse(otraVista.estaVacio());
        assertTrue(servicio.eliminarCotizacion(15));
        assertNull(otraVista.buscarCotizacionPorId(15));
        assertFalse(servicio.eliminarCotizacion(15));
        assertEquals(0, otraVista.cantidadCotizaciones());
        assertTrue(otraVista.obtenerCotizaciones().isEmpty());
        assertEquals(1, repositorio.inserciones);
    }

    @Test
    void idsInvalidosYAusentesNoProvocanEscrituras() {
        assertNull(servicio.buscarCotizacionPorId(-1));
        assertNull(servicio.buscarCotizacionPorId(999));
        assertFalse(servicio.eliminarCotizacion(0));
        assertFalse(servicio.modificarCotizacion(-1, cliente, viaje, plan, 150));
        assertFalse(servicio.modificarCotizacion(999, cliente, viaje, plan, 150));
        assertEquals(0, repositorio.inserciones);
        assertTrue(repositorio.datos.isEmpty());
    }

    @Test
    void rechazaDatosInvalidosAntesDeGuardar() {
        assertThrows(NullPointerException.class, () -> new CotizacionServicio(null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(null, viaje, plan, 150));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(new Cliente("Ana", 25), viaje, plan, 150));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(cliente, null, plan, 150));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(cliente, new Viaje(null, 3), plan, 150));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(cliente, new Viaje(TipoDestino.NACIONAL, 0), plan, 150));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCotizacion(cliente, viaje, null, 150));
        for (double precio : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> servicio.registrarCotizacion(cliente, viaje, plan, precio));
        }
        assertEquals(0, repositorio.inserciones);
    }

    @Test
    void propagaPersistenciaExceptionSinReintentarNiSimularExito() {
        repositorio.error = new PersistenciaException("Fallo simulado", new SQLException("Sin conexion"));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class,
                () -> servicio.registrarCotizacion(cliente, viaje, plan, 150)));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class, servicio::obtenerCotizaciones));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class,
                () -> servicio.buscarCotizacionPorId(15)));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class,
                () -> servicio.modificarCotizacion(15, cliente, viaje, plan, 150)));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class,
                () -> servicio.eliminarCotizacion(15)));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class, servicio::cantidadCotizaciones));
        assertSame(repositorio.error, assertThrows(PersistenciaException.class, servicio::estaVacio));
        assertEquals(1, repositorio.inserciones);
        assertTrue(repositorio.datos.isEmpty());
    }

    /** Doble de prueba; no se utiliza en la aplicacion ni sustituye pruebas MySQL. */
    private static class RepositorioPrueba implements CotizacionRepository {
        final Map<Integer, Cotizacion> datos = new LinkedHashMap<>();
        int inserciones;
        Cotizacion ultimaNueva;
        PersistenciaException error;

        private void comprobarConexion() {
            if (error != null) throw error;
        }

        public Cotizacion guardar(Cotizacion c) {
            inserciones++;
            comprobarConexion();
            ultimaNueva = c;
            Cotizacion guardada = new Cotizacion(14 + inserciones, c.getCliente(),
                    c.getViaje(), c.getPlan(), c.getPrecioFinal());
            datos.put(guardada.getId(), guardada);
            return guardada;
        }

        public List<Cotizacion> listar() {
            comprobarConexion();
            return new ArrayList<>(datos.values());
        }

        public Optional<Cotizacion> buscarPorId(int id) {
            comprobarConexion();
            return Optional.ofNullable(datos.get(id));
        }

        public boolean actualizar(Cotizacion c) {
            comprobarConexion();
            return datos.replace(c.getId(), c) != null;
        }

        public boolean eliminar(int id) {
            comprobarConexion();
            return datos.remove(id) != null;
        }

        public int contar() {
            comprobarConexion();
            return datos.size();
        }
    }
}
