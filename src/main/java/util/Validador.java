package util;

public class Validador {

    /**
     * Verifica que un texto contenga información válida.
     *
     * @param texto texto que se desea validar
     * @return true si el texto no es nulo ni está vacío, false en caso contrario
     */
    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Verifica que un nombre contenga solamente letras y espacios.
     *
     * @param nombre nombre que se desea validar
     * @return true si el nombre contiene solamente letras y espacios,
     * false en caso contrario
     */
    public static boolean esNombreValido(String nombre) {
        if (!esTextoValido(nombre)) {
            return false;
        }

        return nombre.trim().matches("[\\p{L} ]+");
    }

    /**
     * Verifica si un texto representa un número entero.
     *
     * @param texto texto que se desea validar
     * @return true si el texto puede convertirse a entero, false en caso contrario
     */
    public static boolean esEntero(String texto) {
        if (!esTextoValido(texto)) {
            return false;
        }

        try {
            Integer.parseInt(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifica que una edad ingresada sea un número entero mayor que cero.
     *
     * @param textoEdad edad recibida como texto
     * @return true si la edad es válida, false en caso contrario
     */
    public static boolean esEdadValida(String textoEdad) {
        if (!esEntero(textoEdad)) {
            return false;
        }

        int edad = Integer.parseInt(textoEdad);
        return edad > 0;
    }

    /**
     * Verifica que la cantidad de días ingresada sea un número entero mayor que cero.
     *
     * @param textoDias cantidad de días recibida como texto
     * @return true si la cantidad de días es válida, false en caso contrario
     */
    public static boolean esCantidadDiasValida(String textoDias) {
        if (!esEntero(textoDias)) {
            return false;
        }

        int dias = Integer.parseInt(textoDias);
        return dias > 0;
    }
}