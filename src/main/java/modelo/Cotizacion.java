package modelo;

/**
 * Representa una cotizacion de seguro de viaje contratada por un cliente.
 *
 * Almacena el cliente asociado, los datos del viaje, el plan seleccionado
 * y el precio final calculado para la contratacion.
 */
public class Cotizacion {

    private static int contadorId = 1;

    private final int id;
    private final Cliente cliente;
    private final Viaje viaje;
    private final PlanSeguro plan;
    private final double precioFinal;

    /**
     * Crea una nueva cotizacion y le asigna automaticamente
     * un identificador unico.
     *
     * @param cliente cliente que realiza la contratacion
     * @param viaje viaje asociado a la cotizacion
     * @param plan plan de seguro seleccionado
     * @param precioFinal precio final calculado para el viaje
     */
    public Cotizacion(
            Cliente cliente,
            Viaje viaje,
            PlanSeguro plan,
            double precioFinal
    ) {
        this.id = contadorId++;
        this.cliente = cliente;
        this.viaje = viaje;
        this.plan = plan;
        this.precioFinal = precioFinal;
    }

    /**
     * Obtiene el identificador de la cotizacion.
     *
     * @return identificador unico de la cotizacion
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el cliente asociado a la cotizacion.
     *
     * @return cliente que realizo la contratacion
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Obtiene los datos del viaje cotizado.
     *
     * @return viaje asociado a la cotizacion
     */
    public Viaje getViaje() {
        return viaje;
    }

    /**
     * Obtiene el plan de seguro contratado.
     *
     * @return plan seleccionado
     */
    public PlanSeguro getPlan() {
        return plan;
    }

    /**
     * Obtiene el precio final de la cotizacion.
     *
     * @return precio final calculado
     */
    public double getPrecioFinal() {
        return precioFinal;
    }
}