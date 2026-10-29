package modelo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Estadisticas globales inmutables calculadas a partir de los precios historicos.
 *
 * @param totalCotizaciones cantidad total de registros
 * @param sumaPreciosFinales suma decimal de precio_final, cero si no hay registros
 * @param cantidadesPorPlan cantidades por ID del catalogo (1 basico, 2 total, 3 deportes)
 */
public record ResumenCotizaciones(long totalCotizaciones, BigDecimal sumaPreciosFinales,
                                  Map<Integer, Long> cantidadesPorPlan) {
    /** Crea una copia inmutable del desglose y requiere un monto no nulo. */
    public ResumenCotizaciones {
        Objects.requireNonNull(sumaPreciosFinales);
        cantidadesPorPlan = Map.copyOf(cantidadesPorPlan);
    }
}
