package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controla la vista del historial de cotizaciones.
 *
 * En esta fase del proyecto es una pantalla esqueleto que informa
 * al usuario que todavia no hay cotizaciones contratadas.
 */
public class HistorialController {

    // Etiqueta con la informacion del estado del historial.
    @FXML
    private Label mensajeHistorial;

    /**
     * Inicializa la vista del historial configurando el mensaje
     * de estado para el usuario.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {
        mensajeHistorial.setText("Las cotizaciones contratadas apareceran aqui "
                + "cuando se integre la capa de repositorios y servicios.");
    }
}