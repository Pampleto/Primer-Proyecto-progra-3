package repositorio;

import modelo.Cotizacion;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de persistencia disponibles para las cotizaciones.
 */
public interface CotizacionRepository {

    /**
     * Registra una cotizacion nueva y obtiene su identificador persistente.
     * El ID temporal no debe insertarse como ID de MySQL.
     *
     * @param cotizacion cotizacion nueva con sus datos asociados
     * @return cotizacion registrada con el ID generado por MySQL
     */
    Cotizacion guardar(Cotizacion cotizacion);

    /**
     * Consulta todas las cotizaciones registradas.
     *
     * @return lista de cotizaciones, vacia si no existen registros
     */
    List<Cotizacion> listar();

    /**
     * Busca una cotizacion por su identificador persistente.
     *
     * @param id identificador de la cotizacion buscada
     * @return cotizacion encontrada u Optional vacio si no existe
     */
    Optional<Cotizacion> buscarPorId(int id);

    /**
     * Actualiza los datos de una cotizacion existente identificada por su ID.
     *
     * @param cotizacion cotizacion con ID persistente y datos actualizados
     * @return true si se modifico un registro, false en caso contrario
     */
    boolean actualizar(Cotizacion cotizacion);

    /**
     * Elimina una cotizacion por su identificador persistente.
     *
     * @param id identificador de la cotizacion a eliminar
     * @return true si se elimino un registro, false si no existe
     */
    boolean eliminar(int id);

    /**
     * Cuenta las cotizaciones registradas.
     *
     * @return cantidad total de cotizaciones persistidas
     */
    int contar();
}
