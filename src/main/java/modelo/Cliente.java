package modelo;

public class Cliente {

    private static int contador = 1;

    private int id;
    private String nombre;
    private int edad;

    public Cliente(String nombre, int edad) {
        this.id = contador++;
        this.nombre = nombre;
        this.edad = edad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }
}