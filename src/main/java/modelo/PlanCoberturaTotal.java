package modelo;

/**
 * Representa el plan de cobertura total de seguro de viaje.
 *
 * Este plan ofrece una cobertura mas amplia para emergencias,
 * asistencia y gastos medicos.
 */
public class PlanCoberturaTotal extends PlanSeguro {

    /**
     * Crea un nuevo plan de cobertura total
     * con sus valores predeterminados.
     *
     * No recibe parametros.
     * No retorna ningun valor porque es un constructor.
     *
     * Utiliza super para inicializar los datos heredados
     * de la clase PlanSeguro.
     */
    public PlanCoberturaTotal() {
        super(
                "Cobertura Total",
                "Cobertura amplia para emergencias, asistencia y gastos medicos",
                7500.0
        );
    }

    /**
     * Calcula el precio final del plan de cobertura total.
     *
     * Recibe un objeto Viaje con el destino
     * y la cantidad de dias.
     *
     * Multiplica la tarifa diaria por los dias del viaje.
     * Si el destino es internacional, el precio se multiplica por dos.
     *
     * Retorna el precio final calculado.
     *
     * @param viaje viaje que se desea cotizar
     * @return precio final del plan de cobertura total
     */
    @Override
    public double calcularPrecio(Viaje viaje) {

        // Se calcula el precio base de acuerdo con los dias.
        double precio = tarifaDiaria * viaje.getCantidadDias();

        // Los viajes internacionales tienen un costo mayor.
        if (viaje.getDestino() == TipoDestino.INTERNACIONAL) {
            precio = precio * 2;
        }

        // Se devuelve el precio final calculado.
        return precio;
    }
}