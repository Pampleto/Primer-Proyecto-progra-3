package servicio;

import modelo.PlanBasico;
import modelo.PlanCoberturaTotal;
import modelo.PlanDeportesExtremos;
import modelo.PlanSeguro;
import modelo.Viaje;
import util.Validador;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de cotizacion de la aplicacion TravelSafe.
 *
 * Recibe un viaje y calcula de forma simultanea el precio final
 * de los tres planes de seguro disponibles en el sistema.
 *
 * Funciona como intermediario entre el controlador del cotizador
 * y los planes de cobertura, centralizando la logica de cotizacion
 * en la capa de servicios.
 */
public class CotizadorServicio {

    // Planes de seguro disponibles en el sistema en orden fijo.
    private final List<PlanSeguro> planes;

    /**
     * Crea el motor de cotizacion cargando los tres planes disponibles.
     *
     * No recibe parametros.
     * No retorna ningun valor porque es un constructor.
     */
    public CotizadorServicio() {
        planes = List.of(
                new PlanBasico(),
                new PlanCoberturaTotal(),
                new PlanDeportesExtremos()
        );
    }

    /**
     * Obtiene los planes de seguro disponibles en el sistema.
     *
     * No recibe parametros.
     * Retorna la lista de planes en el orden basico,
     * cobertura total y deportes extremos.
     *
     * @return lista de planes de seguro disponibles
     */
    public List<PlanSeguro> obtenerPlanes() {
        return planes;
    }

    /**
     * Calcula el precio final de los tres planes para el viaje recibido.
     *
     * Recibe un objeto Viaje con el destino y la cantidad de dias.
     * Valida que el viaje, el destino y la cantidad de dias sean
     * correctos antes de realizar el calculo.
     *
     * Retorna un mapa ordenado que contiene el nombre de cada plan
     * como clave y su precio final como valor, calculados de forma
     * simultanea con el mismo viaje.
     *
     * @param viaje viaje que se desea cotizar
     * @return mapa ordenado con los precios de los tres planes
     * @throws IllegalArgumentException si el viaje o los datos son invalidos
     */
    public Map<String, Double> cotizar(Viaje viaje) {
        if (viaje == null) {
            throw new IllegalArgumentException("El viaje no puede ser nulo.");
        }

        if (viaje.getDestino() == null) {
            throw new IllegalArgumentException("El destino del viaje no puede ser nulo.");
        }

        if (!Validador.esCantidadDiasValida(String.valueOf(viaje.getCantidadDias()))) {
            throw new IllegalArgumentException(
                    "La cantidad de dias debe ser un numero entero mayor que cero."
            );
        }

        // Se recorre cada plan y se calcula su precio con el viaje recibido.
        Map<String, Double> precios = new LinkedHashMap<>();
        for (PlanSeguro plan : planes) {
            precios.put(plan.getNombre(), plan.calcularPrecio(viaje));
        }

        // Se devuelven los tres precios calculados simultaneamente.
        return precios;
    }
}