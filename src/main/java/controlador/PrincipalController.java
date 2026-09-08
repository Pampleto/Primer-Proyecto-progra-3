package controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controla la ventana principal de la aplicacion TravelSafe.
 *
 * Administra el menu lateral y carga dinamicamente las vistas
 * de clientes, cotizador e historial en el panel central.
 */
public class PrincipalController {

    // Panel central donde se muestran las vistas cargadas.
    @FXML
    private StackPane contenidoCentral;

    // Guarda las vistas ya cargadas para conservar su estado
    // al navegar de una pantalla a otra.
    private final Map<String, Node> vistas = new HashMap<>();

    /**
     * Inicializa la ventana principal cargando la vista de clientes
     * como pantalla inicial.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {
        mostrarVista("/vista/clientes.fxml");
    }

    /**
     * Muestra la vista de registro de clientes en el panel central.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void mostrarClientes() {
        mostrarVista("/vista/clientes.fxml");
    }

    /**
     * Muestra la vista del cotizador de planes en el panel central.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void mostrarCotizador() {
        mostrarVista("/vista/cotizador.fxml");
    }

    /**
     * Muestra la vista del historial en el panel central.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void mostrarHistorial() {
        mostrarVista("/vista/historial.fxml");
    }

    /**
     * Carga una vista FXML dentro del panel central de la aplicacion.
     *
     * Si la vista ya fue cargada anteriormente se reutiliza la instancia
     * guardada, de esta manera se conserva la informacion ingresada
     * en cada pantalla mientras se navega.
     *
     * @param rutaFxml ruta del archivo FXML dentro de la carpeta resources
     */
    private void mostrarVista(String rutaFxml) {
        try {
            Node vista = vistas.get(rutaFxml);
            if (vista == null) {
                vista = new FXMLLoader(getClass().getResource(rutaFxml)).load();
                vistas.put(rutaFxml, vista);
            }
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            // En la version final este error deberia notificarse al usuario
            // en la interfaz, y no simplemente imprimirse en consola.
            e.printStackTrace();
        }
    }
}