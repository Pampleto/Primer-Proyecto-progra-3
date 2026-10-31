package util;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * Utilidad para mostrar diálogos modales al usuario.
 *
 * Centraliza la creación de Alert de JavaFX para mantener
 * consistentes los mensajes de error, información y advertencia
 * en todas las vistas de la aplicación TravelSafe.
 */
public final class Dialogos {

    // Evita la instanciación de esta clase utilitaria.
    private Dialogos() {
    }

    /**
     * Muestra un diálogo modal de error.
     *
     * @param titulo  titulo de la ventana
     * @param mensaje descripcion del error para el usuario
     */
    public static void mostrarError(
            String titulo,
            String mensaje
    ) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje, null);
    }

    /**
     * Muestra un diálogo modal de información.
     *
     * @param titulo  titulo de la ventana
     * @param mensaje descripcion informativa para el usuario
     */
    public static void mostrarInformacion(
            String titulo,
            String mensaje
    ) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje, null);
    }

    /**
     * Muestra un diálogo modal de advertencia.
     *
     * @param titulo  titulo de la ventana
     * @param mensaje descripcion de la advertencia
     */
    public static void mostrarAdvertencia(
            String titulo,
            String mensaje
    ) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje, null);
    }

    /**
     * Construye y muestra un Alert bloqueante.
     *
     * @param tipo       nivel del mensaje (ERROR, INFORMATION, WARNING)
     * @param titulo     titulo de la ventana
     * @param mensaje    contenido mostrado al usuario
     * @param propietario ventana dueña; null si no hay una activa
     */
    private static void mostrar(
            Alert.AlertType tipo,
            String titulo,
            String mensaje,
            Window propietario
    ) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        if (propietario != null) {
            alerta.initOwner(propietario);
        }
        alerta.showAndWait();
    }
}
