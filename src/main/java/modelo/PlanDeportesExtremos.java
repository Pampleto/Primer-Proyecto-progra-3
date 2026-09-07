package modelo;

/**
 * Representa el plan de seguro para deportes extremos.
 *
 * Este plan esta diseñado para actividades de mayor riesgo
 * y utiliza la tarifa diaria mas alta de los tres planes.
 */
public class PlanDeportesExtremos extends PlanSeguro {

    /**
     * Crea un nuevo plan de deportes extremos
     * con sus valores predeterminados.
     *
     * No recibe parametros.
     * No retorna ningun valor porque es un constructor.
     *
     * Utiliza super para inicializar los atributos heredados
     * de la clase PlanSeguro.
     */
    public PlanDeportesExtremos() {
        super(
                "Deportes Extremos",
                "Cobertura especial para actividades de mayor riesgo",
                12000.0
        );
    }

    /**
     * Calcula el precio final del plan de deportes extremos.
     *
     * Recibe un objeto Viaje que contiene el destino
     * y la cantidad de dias.
     *
     * Multiplica la tarifa diaria por la cantidad de dias.
     * Si el destino es internacional, el resultado
     * se multiplica por dos.
     *
     * Retorna el precio final calculado.
     *
     * @param viaje viaje que se desea cotizar
     * @return precio final del plan de deportes extremos
     */
    @Override
    public double calcularPrecio(Viaje viaje) {

        // Se calcula el precio base segun la duracion del viaje.
        double precio = tarifaDiaria * viaje.getCantidadDias();

        // Si el viaje es internacional se duplica el precio.
        if (viaje.getDestino() == TipoDestino.INTERNACIONAL) {
            precio = precio * 2;
        }

        // Se devuelve el precio final de la cotizacion.
        return precio;
    }
}