package interfaces;

import modelo.Viaje;

/**
 * Define el comportamiento que debe tener cualquier objeto
 * que pueda calcular una cotizacion para un viaje.
 */
public interface Cotizable {

    /**
     * Calcula el precio correspondiente a un viaje determinado.
     *
     * Recibe un objeto Viaje con el destino y la cantidad de dias.
     * Retorna el precio final calculado.
     *
     * @param viaje viaje utilizado para realizar la cotizacion
     * @return precio final calculado
     */
    double calcularPrecio(Viaje viaje);
}