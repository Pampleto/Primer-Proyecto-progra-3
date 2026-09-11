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
import servicio.ClienteServicio;

/**
 * Controla la vista de gestion de clientes.
 *
 * Permite capturar los datos de un cliente, solicitar su registro
 * mediante ClienteServicio y mostrar los clientes registrados
 * dentro de una tabla.
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

    // Tabla donde se muestran los clientes registrados.
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

    // Servicio encargado de gestionar las operaciones de los clientes.
    private final ClienteServicio clienteServicio = new ClienteServicio();

    // Lista observable utilizada solamente para mostrar los clientes en la tabla.
    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    /**
     * Inicializa la vista y configura las columnas de la tabla
     * para mostrar los datos del modelo Cliente.
     *
     * Tambien carga en la tabla los clientes almacenados
     * mediante ClienteServicio.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {

        columnaId.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getId()
                )
        );

        columnaNombre.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getNombre()
                )
        );

        columnaEdad.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getEdad()
                )
        );

        tablaClientes.setItems(clientes);

        actualizarTabla();
    }

    /**
     * Registra un nuevo cliente utilizando los datos ingresados
     * en los campos del formulario.
     *
     * Envia el nombre y la edad a ClienteServicio, donde se realizan
     * las validaciones y se almacena el cliente en el repositorio.
     *
     * Si el registro es correcto, actualiza la tabla y limpia
     * los campos del formulario. Si ocurre un error de validacion,
     * muestra el mensaje correspondiente en la interfaz.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void registrarCliente() {

        String nombre = campoNombre.getText();
        String edad = campoEdad.getText();

        try {

            clienteServicio.registrarCliente(nombre, edad);

            actualizarTabla();

            mensajeEstado.setText(
                    "Cliente registrado correctamente."
            );

            campoNombre.clear();
            campoEdad.clear();

        } catch (IllegalArgumentException e) {

            mensajeEstado.setText(e.getMessage());
        }
    }

    /**
     * Actualiza la informacion mostrada en la tabla de clientes.
     *
     * Obtiene todos los clientes almacenados mediante ClienteServicio
     * y los coloca dentro de la lista observable utilizada por la tabla.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void actualizarTabla() {

        clientes.setAll(
                clienteServicio.obtenerClientes()
        );
    }
}