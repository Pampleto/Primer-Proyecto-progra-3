package servicio;

import modelo.Cliente;
import repositorio.Repositorio;
import util.Validador;

import java.util.List;

/**
 * Gestiona las operaciones relacionadas con los clientes.
 *
 * Esta clase funciona como intermediaria entre el controlador
 * de la interfaz y el repositorio generico.
 *
 * Permite registrar, consultar y modificar clientes utilizando
 * las validaciones creadas en la clase Validador.
 */
public class ClienteServicio {

    // Repositorio generico utilizado para almacenar objetos Cliente.
    private final Repositorio<Cliente> repositorioClientes;

    /**
     * Crea el servicio de clientes e inicializa
     * el repositorio donde se almacenaran.
     *
     * No recibe parametros.
     * No retorna ningun valor porque es un constructor.
     */
    public ClienteServicio() {
        repositorioClientes = new Repositorio<>();
    }

    /**
     * Registra un nuevo cliente utilizando el nombre y la edad recibidos.
     *
     * Primero valida que el nombre contenga solamente letras y espacios
     * y que la edad sea un numero entero mayor que cero.
     *
     * Recibe el nombre y la edad del cliente como texto.
     * Retorna el objeto Cliente que fue registrado.
     *
     * @param nombre nombre del cliente
     * @param edadTexto edad del cliente recibida como texto
     * @return cliente registrado correctamente
     * @throws IllegalArgumentException si el nombre o la edad no son validos
     */
    public Cliente registrarCliente(String nombre, String edadTexto) {

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

        int edad = Integer.parseInt(edadTexto);

        Cliente cliente = new Cliente(nombre.trim(), edad);

        repositorioClientes.agregar(cliente);

        return cliente;
    }

    /**
     * Obtiene todos los clientes almacenados actualmente
     * dentro del repositorio.
     *
     * No recibe parametros.
     * Retorna una lista con todos los clientes registrados.
     *
     * @return lista de clientes registrados
     */
    public List<Cliente> obtenerClientes() {
        return repositorioClientes.obtenerTodos();
    }

    /**
     * Busca un cliente utilizando su identificador unico.
     *
     * Recorre los clientes almacenados en el repositorio
     * hasta encontrar uno cuyo ID coincida con el recibido.
     *
     * Recibe el identificador del cliente que se desea buscar.
     * Retorna el cliente encontrado o null si no existe.
     *
     * @param id identificador del cliente
     * @return cliente encontrado o null si no existe
     */
    public Cliente buscarClientePorId(int id) {

        for (Cliente cliente : repositorioClientes.obtenerTodos()) {

            if (cliente.getId() == id) {
                return cliente;
            }
        }

        return null;
    }

    /**
     * Modifica el nombre y la edad de un cliente existente.
     *
     * Primero valida que el nombre contenga solamente letras y espacios
     * y que la edad sea un numero entero mayor que cero.
     *
     * Recibe el ID del cliente, el nuevo nombre y la nueva edad.
     * Retorna true si el cliente fue modificado correctamente
     * o false si el cliente no fue encontrado.
     *
     * @param id identificador del cliente que se desea modificar
     * @param nombre nuevo nombre del cliente
     * @param edadTexto nueva edad del cliente recibida como texto
     * @return true si el cliente fue modificado, false si no fue encontrado
     * @throws IllegalArgumentException si el nombre o la edad no son validos
     */
    public boolean modificarCliente(
            int id,
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

        Cliente cliente = buscarClientePorId(id);

        if (cliente == null) {
            return false;
        }

        int edad = Integer.parseInt(edadTexto);

        cliente.setNombre(nombre.trim());
        cliente.setEdad(edad);

        return true;
    }

    /**
     * Indica si actualmente existen clientes registrados.
     *
     * No recibe parametros.
     * Retorna true si no existen clientes almacenados
     * o false si hay al menos un cliente registrado.
     *
     * @return true si el repositorio esta vacio, false en caso contrario
     */
    public boolean estaVacio() {
        return repositorioClientes.estaVacio();
    }

    /**
     * Obtiene la cantidad total de clientes registrados.
     *
     * No recibe parametros.
     * Retorna el numero de clientes almacenados en el repositorio.
     *
     * @return cantidad de clientes registrados
     */
    public int cantidadClientes() {
        return repositorioClientes.cantidad();
    }
}