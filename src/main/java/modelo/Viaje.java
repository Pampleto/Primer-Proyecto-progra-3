package modelo;

/**
 * Representa la informacion de un viaje que sera utilizado
 * para calcular el precio de un seguro.
 *
 * Almacena el tipo de destino y la cantidad de dias del viaje.
 */
public class Viaje {

    // Indica si el viaje es nacional o internacional.
    private TipoDestino destino;

    // Cantidad total de dias que durara el viaje.
    private int cantidadDias;

    /**
     * Crea un nuevo viaje con su destino y duracion.
     *
     * Recibe el tipo de destino y la cantidad de dias del viaje.
     * No retorna ningun valor porque es un constructor.
     *
     * @param destino tipo de destino del viaje
     * @param cantidadDias cantidad de dias que durara el viaje
     */
    public Viaje(TipoDestino destino, int cantidadDias) {
        this.destino = destino;
        this.cantidadDias = cantidadDias;
    }

    /**
     * Obtiene el tipo de destino del viaje.
     *
     * No recibe parametros.
     * Retorna el destino actual del viaje.
     *
     * @return tipo de destino del viaje
     */
    public TipoDestino getDestino() {
        return destino;
    }

    /**
     * Modifica el tipo de destino del viaje.
     *
     * Recibe como parametro el nuevo destino.
     * No retorna ningun valor.
     *
     * @param destino nuevo tipo de destino del viaje
     */
    public void setDestino(TipoDestino destino) {
        this.destino = destino;
    }

    /**
     * Obtiene la cantidad de dias del viaje.
     *
     * No recibe parametros.
     * Retorna la cantidad actual de dias.
     *
     * @return cantidad de dias del viaje
     */
    public int getCantidadDias() {
        return cantidadDias;
    }

    /**
     * Modifica la cantidad de dias del viaje.
     *
     * Recibe como parametro la nueva cantidad de dias.
     * No retorna ningun valor.
     *
     * @param cantidadDias nueva cantidad de dias del viaje
     */
    public void setCantidadDias(int cantidadDias) {
        this.cantidadDias = cantidadDias;
    }
}