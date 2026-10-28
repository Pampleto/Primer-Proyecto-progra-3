package modelo;

/**
 * Representa un cliente registrado en TravelSafe.
 * El identificador es generado por MySQL.
 */
public class Cliente {

    private final int id;
    private String nombre;
    private int edad;

    /**
     * Crea un cliente nuevo sin identificador.
     * MySQL asignara su ID cuando se registre.
     *
     * @param nombre nombre del cliente
     * @param edad edad del cliente
     */
    public Cliente(String nombre, int edad) {
        this(0, nombre, edad);
    }

    /**
     * Reconstruye un cliente con su ID existente.
     *
     * @param id identificador almacenado en MySQL
     * @param nombre nombre del cliente
     * @param edad edad del cliente
     */
    public Cliente(int id, String nombre, int edad) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "El ID no puede ser negativo."
            );
        }

        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
    }

    /**
     * @return identificador del cliente
     */
    public int getId() {
        return id;
    }

    /**
     * @return nombre del cliente
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Modifica el nombre del cliente.
     *
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return edad del cliente
     */
    public int getEdad() {
        return edad;
    }

    /**
     * Modifica la edad del cliente.
     *
     * @param edad nueva edad
     */
    public void setEdad(int edad) {
        this.edad = edad;
    }
}
