package servicio;

import modelo.Cliente;
import modelo.Cotizacion;
import modelo.PlanSeguro;
import modelo.Viaje;
import repositorio.Repositorio;

import java.util.List;

/**
 * Gestiona las operaciones relacionadas con las cotizaciones contratadas.
 *
 * Esta clase funciona como intermediaria entre los controladores
 * y el repositorio generico de cotizaciones.
 *
 * Permite registrar, consultar y buscar cotizaciones realizadas
 * por los clientes.
 */
public class CotizacionServicio {

    // Repositorio compartido para que distintas vistas accedan
    // a las mismas cotizaciones registradas.
    private static final Repositorio<Cotizacion> repositorioCotizaciones =
            new Repositorio<>();

    /**
     * Registra una nueva cotizacion utilizando los datos recibidos.
     *
     * @param cliente cliente que realiza la contratacion
     * @param viaje viaje asociado a la cotizacion
     * @param plan plan de seguro seleccionado
     * @param precioFinal precio final calculado
     * @return cotizacion registrada correctamente
     * @throws IllegalArgumentException si alguno de los datos no es valido
     */
    public Cotizacion registrarCotizacion(
            Cliente cliente,
            Viaje viaje,
            PlanSeguro plan,
            double precioFinal
    ) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Debe existir un cliente para realizar la contratacion."
            );
        }

        if (viaje == null) {
            throw new IllegalArgumentException(
                    "Debe existir un viaje para realizar la contratacion."
            );
        }

        if (plan == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un plan de seguro."
            );
        }

        if (precioFinal <= 0) {
            throw new IllegalArgumentException(
                    "El precio final debe ser mayor que cero."
            );
        }

        Cotizacion cotizacion =
                new Cotizacion(cliente, viaje, plan, precioFinal);

        repositorioCotizaciones.agregar(cotizacion);

        return cotizacion;
    }

    /**
     * Obtiene todas las cotizaciones registradas.
     *
     * No recibe parametros.
     * @return lista con las cotizaciones existentes
     */
    public List<Cotizacion> obtenerCotizaciones() {
        return repositorioCotizaciones.obtenerTodos();
    }

    /**
     * Busca una cotizacion utilizando su identificador.
     *
     * @param id identificador de la cotizacion
     * @return cotizacion encontrada o null si no existe
     */
    public Cotizacion buscarCotizacionPorId(int id) {

        for (Cotizacion cotizacion : repositorioCotizaciones.obtenerTodos()) {

            if (cotizacion.getId() == id) {
                return cotizacion;
            }
        }

        return null;
    }

    /**
     * Indica si actualmente no existen cotizaciones registradas.
     *
     * No recibe parametros.
     * @return true si el repositorio esta vacio, false en caso contrario
     */
    public boolean estaVacio() {
        return repositorioCotizaciones.estaVacio();
    }

    /**
     * Obtiene la cantidad total de cotizaciones registradas.
     *
     * No recibe parametros.
     * @return número de cotizaciones almacenadas
     */
    public int cantidadCotizaciones() {
        return repositorioCotizaciones.cantidad();
    }
}