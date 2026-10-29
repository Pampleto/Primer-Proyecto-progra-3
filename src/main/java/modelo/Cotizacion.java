package modelo;

/**
 * Representa una cotizacion de seguro de viaje contratada por un cliente.
 *
 * Almacena el cliente asociado, los datos del viaje, el plan seleccionado
 * y el precio final calculado para la contratacion.
 */
public class Cotizacion {

    private static int contadorId = -1;

    private final int id;
    private final Cliente cliente;
    private final Viaje viaje;
    private final PlanSeguro plan;
    private final double precioFinal;

    /**
     * Crea una nueva cotizacion y le asigna automaticamente
     * un identificador temporal negativo, unico durante esta ejecucion.
     * Los IDs positivos quedan reservados para MySQL.
     *
     * @param cliente cliente que realiza la contratacion
     * @param viaje viaje asociado a la cotizacion
     * @param plan plan de seguro seleccionado
     * @param precioFinal precio final calculado para el viaje
     * @throws IllegalStateException si se agotan los identificadores temporales
     */
    public Cotizacion(
            Cliente cliente,
            Viaje viaje,
            PlanSeguro plan,
            double precioFinal
    ) {
        this.id = generarIdTemporal();
        this.cliente = cliente;
        this.viaje = viaje;
        this.plan = plan;
        this.precioFinal = precioFinal;
    }

    /**
     * Recupera una cotizacion con el identificador real asignado por MySQL.
     * No consume ni modifica los identificadores temporales en memoria.
     *
     * @param id identificador positivo almacenado en MySQL
     * @param cliente cliente asociado a la cotizacion
     * @param viaje viaje asociado a la cotizacion
     * @param plan plan de seguro seleccionado
     * @param precioFinal precio historico almacenado para la cotizacion
     * @throws IllegalArgumentException si el identificador no es positivo
     */
    public Cotizacion(
            int id,
            Cliente cliente,
            Viaje viaje,
            PlanSeguro plan,
            double precioFinal
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de MySQL debe ser positivo.");
        }
        this.id = id;
        this.cliente = cliente;
        this.viaje = viaje;
        this.plan = plan;
        this.precioFinal = precioFinal;
    }

    /**
     * Genera un ID temporal sin colisiones entre hilos ni desbordamientos.
     *
     * @return siguiente identificador negativo disponible
     * @throws IllegalStateException si se agotaron los identificadores negativos
     */
    private static synchronized int generarIdTemporal() {
        if (contadorId == 0) {
            throw new IllegalStateException("Se agotaron los IDs temporales.");
        }
        int siguienteId = contadorId;
        contadorId = contadorId == Integer.MIN_VALUE ? 0 : contadorId - 1;
        return siguienteId;
    }

    /**
     * Obtiene el identificador de la cotizacion.
     *
     * @return ID temporal negativo o ID positivo asignado por MySQL
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
