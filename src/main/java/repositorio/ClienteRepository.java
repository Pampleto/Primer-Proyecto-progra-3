package repositorio;

import modelo.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de persistencia
 * disponibles para los clientes.
 */
public interface ClienteRepository {

    /**
     * @param cliente cliente nuevo
     * @return cliente registrado con su ID
     */
    Cliente guardar(Cliente cliente);

    /**
     * @return lista de clientes registrados
     */
    List<Cliente> listar();

    /**
     * @param id identificador buscado
     * @return cliente si existe
     */
    Optional<Cliente> buscarPorId(int id);

    /**
     * @param cliente datos actualizados
     * @return true si se modifico
     */
    boolean actualizar(Cliente cliente);

    /**
     * @param id identificador a eliminar
     * @return true si se elimino
     */
    boolean eliminar(int id);

    /**
     * @return cantidad de clientes
     */
    int contar();
}
