package interfaces;

import modelo.Viaje;

public interface Cotizable {

    /**
     * Calcula el precio del seguro según los datos del viaje.
     *
     * @param viaje viaje utilizado para realizar la cotización
     * @return precio calculado del seguro
     */
    double calcularPrecio(Viaje viaje);
}