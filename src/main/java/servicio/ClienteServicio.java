package servicio;

import modelo.Cliente;
import repositorio.ClienteRepository;
import repositorio.ClienteRepositoryJdbc;
import util.Validador;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona las operaciones de clientes.
 * Valida los datos y utiliza el repositorio JDBC.
 */
public class ClienteServicio {

    private final ClienteRepository repositorioClientes;

    /**
     * Inicializa el servicio con persistencia MySQL.
     */
    public ClienteServicio() {
        this(new ClienteRepositoryJdbc());
    }

    /**
     * Permite utilizar otra implementacion de repositorio.
     *
     * @param repositorioClientes repositorio a utilizar
     */
    public ClienteServicio(
            ClienteRepository repositorioClientes
    ) {
        this.repositorioClientes =
                Objects.requireNonNull(repositorioClientes);
    }

    /**
     * Valida y registra un nuevo cliente.
     *
     * @param nombre nombre ingresado
     * @param edadTexto edad ingresada como texto
     * @return cliente registrado con su ID
     */
    public Cliente registrarCliente(
            String nombre,
            String edadTexto
    ) {

        validarDatos(nombre, edadTexto);

        Cliente nuevo = new Cliente(
                nombre.trim(),
                Integer.parseInt(edadTexto)
        );

        return repositorioClientes.guardar(nuevo);
    }

    /**
     * Obtiene los clientes de MySQL.
     *
     * @return lista de clientes registrados
     */
    public List<Cliente> obtenerClientes() {
        return repositorioClientes.listar();
    }

    /**
     * Busca un cliente por su ID.
     *
     * @param id identificador del cliente
     * @return cliente encontrado o null
     */
    public Cliente buscarClientePorId(int id) {

        if (id <= 0) {
            return null;
        }

        return repositorioClientes
                .buscarPorId(id)
                .orElse(null);
    }

    /**
     * Modifica los datos de un cliente.
     *
     * @param id identificador
     * @param nombre nuevo nombre
     * @param edadTexto nueva edad
     * @return true si se modifico
     */
    public boolean modificarCliente(
            int id,
            String nombre,
            String edadTexto
    ) {

        validarDatos(nombre, edadTexto);

        if (id <= 0) {
            return false;
        }

        Cliente actualizado = new Cliente(
                id,
                nombre.trim(),
                Integer.parseInt(edadTexto)
        );

        return repositorioClientes.actualizar(actualizado);
    }

    /**
     * Elimina un cliente por identificador.
     *
     * @param id identificador del cliente
     * @return true si se elimino
     */
    public boolean eliminarCliente(int id) {

        if (id <= 0) {
            return false;
        }

        return repositorioClientes.eliminar(id);
    }

    /**
     * @return true si no existen clientes registrados
     */
    public boolean estaVacio() {
        return repositorioClientes.contar() == 0;
    }

    /**
     * @return cantidad de clientes en MySQL
     */
    public int cantidadClientes() {
        return repositorioClientes.contar();
    }

    /**
     * Valida el nombre y la edad recibidos.
     *
     * @param nombre nombre del cliente
     * @param edadTexto edad escrita como texto
     */
    private void validarDatos(
            String nombre,
            String edadTexto
    ) {

        if (!Validador.esNombreValido(nombre)) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras y espacios."
            );
        }

        if (!Validador.esEdadValida(edadTexto)) {
            throw new IllegalArgumentException(
                    "La edad debe ser un numero entero mayor que cero."
            );
        }
    }
}
