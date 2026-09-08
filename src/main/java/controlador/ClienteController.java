package controlador;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Cliente;

/**
 * Controla la vista de registro de clientes.
 *
 * Permite capturar el nombre y la edad de un cliente,
 * registrarlo en una lista en memoria y mostrarlo
 * en una tabla.
 */
public class ClienteController {

    // Campo donde se escribe el nombre del cliente.
    @FXML
    private TextField campoNombre;

    // Campo donde se escribe la edad del cliente.
    @FXML
    private TextField campoEdad;

    // Etiqueta que muestra mensajes de error o exito.
    @FXML
    private Label mensajeEstado;

    // Tabla donde se listan los clientes registrados.
    @FXML
    private TableView<Cliente> tablaClientes;

    // Columna que muestra el identificador del cliente.
    @FXML
    private TableColumn<Cliente, Integer> columnaId;

    // Columna que muestra el nombre del cliente.
    @FXML
    private TableColumn<Cliente, String> columnaNombre;

    // Columna que muestra la edad del cliente.
    @FXML
    private TableColumn<Cliente, Integer> columnaEdad;

    // Lista observable con los clientes registrados en memoria.
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();

    /**
     * Inicializa la vista de clientes configurando las columnas
     * de la tabla con los datos del modelo Cliente.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {
        columnaId.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getId()));
        columnaNombre.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getNombre()));
        columnaEdad.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getEdad()));
        tablaClientes.setItems(clientes);
    }

    /**
     * Registra un nuevo cliente con los datos del formulario.
     *
     * Valida que el nombre no este vacio y que la edad sea un numero
     * entero positivo. Si los datos son correctos crea un objeto Cliente,
     * lo agrega a la lista y limpia el formulario.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void registrarCliente() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            mensajeEstado.setText("Debe ingresar el nombre del cliente.");
            return;
        }
        try {
            int edad = Integer.parseInt(campoEdad.getText().trim());
            if (edad <= 0) {
                mensajeEstado.setText("La edad debe ser un numero mayor que cero.");
                return;
            }
            clientes.add(new Cliente(nombre, edad));
            mensajeEstado.setText("Cliente registrado correctamente.");
            campoNombre.clear();
            campoEdad.clear();
        } catch (NumberFormatException e) {
            mensajeEstado.setText("La edad debe ser un numero entero.");
        }
    }
}