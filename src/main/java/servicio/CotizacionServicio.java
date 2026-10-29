package servicio;

import modelo.Cliente;
import modelo.Cotizacion;
import modelo.PlanSeguro;
import modelo.Viaje;
import modelo.FiltroCotizaciones;
import modelo.ResumenCotizaciones;
import repositorio.CotizacionRepository;
import repositorio.CotizacionRepositoryJdbc;
import repositorio.PersistenciaException;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona las operaciones relacionadas con las cotizaciones contratadas.
 *
 * Esta clase funciona como intermediaria entre los controladores
 * y el repositorio de cotizaciones persistidas en MySQL.
 *
 * Permite registrar, consultar, modificar y eliminar cotizaciones.
 * Propaga PersistenciaException al consumidor, sin ocultar fallos de MySQL
 * ni mantener una copia alternativa en memoria.
 */
public class CotizacionServicio {

    private final CotizacionRepository repositorioCotizaciones;

    /**
     * Inicializa el servicio con persistencia JDBC para los controladores actuales.
     */
    public CotizacionServicio() {
        this(new CotizacionRepositoryJdbc());
    }

    /**
     * Permite inyectar una implementacion del repositorio.
     *
     * @param repositorioCotizaciones unica fuente de datos del servicio
     * @throws NullPointerException si el repositorio es nulo
     */
    public CotizacionServicio(CotizacionRepository repositorioCotizaciones) {
        this.repositorioCotizaciones = Objects.requireNonNull(repositorioCotizaciones);
    }

    /**
     * Registra una nueva cotizacion utilizando los datos recibidos.
     *
     * @param cliente cliente que realiza la contratacion
     * @param viaje viaje asociado a la cotizacion
     * @param plan plan de seguro seleccionado
     * @param precioFinal precio final calculado
     * @return cotizacion registrada correctamente
     * @throws IllegalArgumentException si alguno de los datos no es valido
     * @throws PersistenciaException si falla el registro en MySQL
     */
    public Cotizacion registrarCotizacion(
            Cliente cliente,
            Viaje viaje,
            PlanSeguro plan,
            double precioFinal
    ) {

        validarDatos(cliente, viaje, plan, precioFinal);
        Cotizacion cotizacion = new Cotizacion(cliente, viaje, plan, precioFinal);
        // Una sola insercion por llamada; se conserva el ID devuelto por MySQL.
        return repositorioCotizaciones.guardar(cotizacion);
    }

    /**
     * Valida los datos antes de registrar o modificar una cotizacion.
     * El precio recibido es el confirmado por el consumidor; no se recalcula.
     *
     * @param cliente cliente persistido
     * @param viaje viaje con destino y duracion validos
     * @param plan plan seleccionado
     * @param precioFinal precio confirmado
     * @throws IllegalArgumentException si los datos son invalidos
     */
    private void validarDatos(Cliente cliente, Viaje viaje, PlanSeguro plan, double precioFinal) {
        if (cliente == null || cliente.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Debe existir un cliente persistido para realizar la contratacion."
            );
        }

        if (viaje == null || viaje.getDestino() == null || viaje.getCantidadDias() <= 0) {
            throw new IllegalArgumentException(
                    "Debe existir un viaje con destino y dias positivos."
            );
        }

        if (plan == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un plan de seguro."
            );
        }

        if (!Double.isFinite(precioFinal) || precioFinal <= 0) {
            throw new IllegalArgumentException(
                    "El precio final debe ser finito y mayor que cero."
            );
        }
    }

    /**
     * Obtiene todas las cotizaciones registradas.
     *
     * No recibe parametros.
     * @return lista con las cotizaciones existentes
     * @throws PersistenciaException si falla la consulta en MySQL
     */
    public List<Cotizacion> obtenerCotizaciones() {
        return repositorioCotizaciones.listar();
    }

    /**
     * Busca una cotizacion utilizando su identificador.
     *
     * @param id identificador de la cotizacion
     * @return cotizacion encontrada o null si no existe
     * @throws PersistenciaException si falla la consulta en MySQL
     */
    public Cotizacion buscarCotizacionPorId(int id) {

        if (id <= 0) {
            return null;
        }
        return repositorioCotizaciones.buscarPorId(id).orElse(null);
    }

    /**
     * Modifica una cotizacion existente conservando su ID y precio confirmado.
     *
     * @param id identificador persistente de la cotizacion
     * @param cliente cliente persistido asociado
     * @param viaje nuevos datos del viaje
     * @param plan plan seleccionado
     * @param precioFinal precio confirmado para la modificacion
     * @return true si el repositorio informa una fila afectada, false en caso contrario
     * @throws IllegalArgumentException si los datos son invalidos
     * @throws PersistenciaException si falla la actualizacion en MySQL
     */
    public boolean modificarCotizacion(int id, Cliente cliente, Viaje viaje,
                                       PlanSeguro plan, double precioFinal) {
        validarDatos(cliente, viaje, plan, precioFinal);
        if (id <= 0) {
            return false;
        }
        return repositorioCotizaciones.actualizar(
                new Cotizacion(id, cliente, viaje, plan, precioFinal));
    }

    /**
     * Elimina exclusivamente la cotizacion indicada.
     *
     * @param id identificador persistente de la cotizacion
     * @return true si se elimino, false si el ID es invalido o no existe
     * @throws PersistenciaException si falla la eliminacion en MySQL
     */
    public boolean eliminarCotizacion(int id) {
        return id > 0 && repositorioCotizaciones.eliminar(id);
    }

    /**
     * Indica si actualmente no existen cotizaciones registradas.
     *
     * No recibe parametros.
     * @return true si el repositorio esta vacio, false en caso contrario
     * @throws PersistenciaException si falla el conteo en MySQL
     */
    public boolean estaVacio() {
        return repositorioCotizaciones.contar() == 0;
    }

    /**
     * Obtiene la cantidad total de cotizaciones registradas.
     *
     * No recibe parametros.
     * @return número de cotizaciones almacenadas
     */
    public int cantidadCotizaciones() {
        return repositorioCotizaciones.contar();
    }

    /**
     * Consulta el historial en MySQL con los filtros presentes combinados con AND.
     *
     * @param filtros cliente, destino y rango inclusivo de fechas; null consulta todos
     * @return lista ordenada por ID, vacia si no hay coincidencias
     * @throws PersistenciaException si falla la consulta
     */
    public List<Cotizacion> buscarCotizaciones(FiltroCotizaciones filtros) {
        return repositorioCotizaciones.buscar(filtros == null ? FiltroCotizaciones.todos() : filtros);
    }

    /**
     * Consulta el resumen global sin recalcular precios ni cargar el historial.
     *
     * @return total, suma historica decimal y cantidades por ID de plan
     * @throws PersistenciaException si falla la consulta
     */
    public ResumenCotizaciones obtenerResumen() {
        return repositorioCotizaciones.obtenerResumen();
    }
}
