package repositorio;

import java.util.ArrayList;
import java.util.List;

public class Repositorio<T> {

    private List<T> elementos;

    /**
     * Crea un repositorio vacío para almacenar elementos del tipo indicado.
     */
    public Repositorio() {
        this.elementos = new ArrayList<>();
    }

    /**
     * Agrega un elemento al repositorio.
     *
     * @param elemento elemento que se desea almacenar
     */
    public void agregar(T elemento) {
        elementos.add(elemento);
    }

    /**
     * Elimina un elemento almacenado en el repositorio.
     *
     * @param elemento elemento que se desea eliminar
     * @return true si el elemento fue eliminado, false si no se encontraba almacenado
     */
    public boolean eliminar(T elemento) {
        return elementos.remove(elemento);
    }

    /**
     * Obtiene todos los elementos almacenados en el repositorio.
     *
     * @return una nueva lista que contiene los elementos registrados
     */
    public List<T> obtenerTodos() {
        return new ArrayList<>(elementos);
    }

    /**
     * Indica si el repositorio no contiene elementos.
     *
     * @return true si el repositorio está vacío, false en caso contrario
     */
    public boolean estaVacio() {
        return elementos.isEmpty();
    }

    /**
     * Obtiene la cantidad de elementos almacenados.
     *
     * @return número de elementos existentes en el repositorio
     */
    public int cantidad() {
        return elementos.size();
    }
}