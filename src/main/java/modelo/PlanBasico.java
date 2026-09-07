package modelo;

/**
 * Representa el plan basico de seguro de viaje.
 *
 * Este plan ofrece una cobertura basica para emergencias
 * y utiliza una tarifa diaria de 4000 colones.
 */
public class PlanBasico extends PlanSeguro {

    /**
     * Crea un nuevo plan basico con sus valores predeterminados.
     *
     * No recibe parametros.
     * No retorna ningun valor porque es un constructor.
     *
     * Utiliza super para enviar el nombre, descripcion
     * y tarifa diaria al constructor de PlanSeguro.
     */
    public PlanBasico() {
        super(
                "Plan Basico",
                "Cobertura basica para emergencias durante el viaje",
                4000.0
        );
    }

    /**
     * Calcula el precio final del plan basico.
     *
     * Recibe un objeto Viaje que contiene el destino
     * y la cantidad de dias.
     *
     * Multiplica la tarifa diaria por la cantidad de dias.
     * Si el destino es internacional, el precio se multiplica por dos.
     *
     * Retorna el precio final calculado.
     *
     * @param viaje viaje que se desea cotizar
     * @return precio final del plan basico
     */
    @Override
    public double calcularPrecio(Viaje viaje) {

        // Se multiplica la tarifa diaria por la duracion del viaje.
        double precio = tarifaDiaria * viaje.getCantidadDias();

        // Si el destino es internacional se duplica el precio.
        if (viaje.getDestino() == TipoDestino.INTERNACIONAL) {
            precio = precio * 2;
        }

        // Se devuelve el precio final de la cotizacion.
        return precio;
    }
}