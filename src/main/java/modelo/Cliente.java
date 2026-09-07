package modelo;

/**
 * Representa a un cliente dentro del sistema de seguros de viaje.
 * Cada cliente posee un identificador unico, un nombre y una edad.
 */
public class Cliente {

    // Contador utilizado para generar automaticamente
    // un identificador diferente para cada cliente.
    private static int contador = 1;

    // Identificador unico del cliente.
    private int id;

    // Nombre del cliente.
    private String nombre;

    // Edad del cliente.
    private int edad;

    /**
     * Crea un nuevo cliente y genera automaticamente su identificador.
     *
     * Recibe el nombre y la edad del cliente.
     * No retorna ningun valor porque es un constructor.
     *
     * @param nombre nombre del cliente
     * @param edad edad del cliente
     */
    public Cliente(String nombre, int edad) {
        this.id = contador++;
        this.nombre = nombre;
        this.edad = edad;
    }

    /**
     * Obtiene el identificador unico del cliente.
     *
     * No recibe parametros.
     * Retorna el identificador del cliente.
     *
     * @return identificador del cliente
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el nombre almacenado del cliente.
     *
     * No recibe parametros.
     * Retorna el nombre actual del cliente.
     *
     * @return nombre del cliente
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Modifica el nombre actual del cliente.
     *
     * Recibe como parametro el nuevo nombre que se desea asignar.
     * No retorna ningun valor.
     *
     * @param nombre nuevo nombre del cliente
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la edad almacenada del cliente.
     *
     * No recibe parametros.
     * Retorna la edad actual del cliente.
     *
     * @return edad del cliente
     */
    public int getEdad() {
        return edad;
    }

    /**
     * Modifica la edad actual del cliente.
     *
     * Recibe como parametro la nueva edad que se desea asignar.
     * No retorna ningun valor.
     *
     * @param edad nueva edad del cliente
     */
    public void setEdad(int edad) {
        this.edad = edad;
    }
}