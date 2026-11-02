package red;

/** Un mensaje UTF-8 terminado en LF, sin datos personales. */
public final class ProtocoloCotizaciones {
    private ProtocoloCotizaciones() { }

    public static String mensaje(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID no positivo");
        return "COTIZACION_CREADA|" + id;
    }

    public static int validar(String mensaje) {
        if (mensaje == null || !mensaje.matches("COTIZACION_CREADA\\|[1-9][0-9]{0,9}")) {
            throw new IllegalArgumentException("Mensaje invalido");
        }
        try {
            return Integer.parseInt(mensaje.substring(mensaje.indexOf('|') + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID fuera de rango", e);
        }
    }
}
