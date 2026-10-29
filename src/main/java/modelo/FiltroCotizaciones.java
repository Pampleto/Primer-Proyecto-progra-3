package modelo;

import java.time.LocalDate;

/**
 * Criterios opcionales combinados con AND para consultar el historial.
 *
 * @param clienteId ID positivo del cliente, o null para todos
 * @param destino destino requerido, o null para ambos
 * @param fechaDesde primera fecha de contratacion incluida, o null sin limite inferior
 * @param fechaHasta ultima fecha de contratacion incluida, o null sin limite superior
 */
public record FiltroCotizaciones(Integer clienteId, TipoDestino destino,
                                 LocalDate fechaDesde, LocalDate fechaHasta) {
    /**
     * Valida los criterios y el rango DATETIME de MySQL.
     *
     * @throws IllegalArgumentException si el ID o las fechas son invalidos
     */
    public FiltroCotizaciones {
        if (clienteId != null && clienteId <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser positivo.");
        }
        validarFecha(fechaDesde);
        validarFecha(fechaHasta);
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede superar la fecha final.");
        }
    }

    /** @return criterios vacios, equivalentes a consultar todo el historial */
    public static FiltroCotizaciones todos() {
        return new FiltroCotizaciones(null, null, null, null);
    }

    /**
     * @param fecha fecha opcional que debe estar dentro de DATETIME
     * @throws IllegalArgumentException si la fecha esta fuera del rango MySQL
     */
    private static void validarFecha(LocalDate fecha) {
        if (fecha != null && (fecha.getYear() < 1000 || fecha.getYear() > 9999)) {
            throw new IllegalArgumentException("La fecha debe estar entre los anios 1000 y 9999.");
        }
    }
}
