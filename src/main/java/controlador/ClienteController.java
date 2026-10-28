package controlador;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Cliente;
import repositorio.PersistenciaException;
import servicio.ClienteServicio;

import java.util.List;

/**
 * Controla la gestion de clientes de TravelSafe.
 * Utiliza los servicios conectados a MySQL.
 */
public class ClienteController {

    // =====================================================
    // COMPONENTES FXML
    // =====================================================

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoEdad;

    @FXML
    private TextField campoBuscarId;

    @FXML
    private Label mensajeEstado;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, Integer> columnaId;

    @FXML
    private TableColumn<Cliente, String> columnaNombre;

    @FXML
    private TableColumn<Cliente, Integer> columnaEdad;

    // =====================================================
    // SERVICIO Y DATOS
    // =====================================================

    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    private Cliente clienteSeleccionado;

    // =====================================================
    // INICIALIZACION
    // =====================================================

    /**
     * Configura la tabla, sus columnas y la seleccion.
     * Carga los clientes utilizando un Task de JavaFX.
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

        tablaClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {

                    if (seleccionado != null) {
                        cargarClienteSeleccionado(seleccionado);
                    } else {
                        clienteSeleccionado = null;
                    }
                });

        cargarClientesEnSegundoPlano();
    }

    // =====================================================
    // REGISTRAR CLIENTE
    // =====================================================

    /**
     * Registra un cliente nuevo en MySQL y actualiza
     * los registros de la tabla.
     */
    @FXML
    protected void registrarCliente() {

        try {

            Cliente registrado =
                    clienteServicio.registrarCliente(
                            campoNombre.getText(),
                            campoEdad.getText()
                    );

            actualizarTabla();
            limpiarFormulario();

            mensajeEstado.setText(
                    "Cliente registrado correctamente. ID: "
                            + registrado.getId()
            );

        } catch (IllegalArgumentException
                 | PersistenciaException e) {

            mensajeEstado.setText(e.getMessage());
        }
    }

    // =====================================================
    // MODIFICAR CLIENTE
    // =====================================================

    /**
     * Modifica el cliente seleccionado y actualiza
     * sus datos almacenados en MySQL.
     */
    @FXML
    protected void modificarCliente() {

        if (clienteSeleccionado == null) {
            mensajeEstado.setText(
                    "Debe seleccionar un cliente de la tabla."
            );
            return;
        }

        try {

            boolean modificado =
                    clienteServicio.modificarCliente(
                            clienteSeleccionado.getId(),
                            campoNombre.getText(),
                            campoEdad.getText()
                    );

            if (modificado) {

                actualizarTabla();
                limpiarFormulario();

                mensajeEstado.setText(
                        "Cliente modificado correctamente."
                );

            } else {

                mensajeEstado.setText(
                        "No se encontro el cliente seleccionado."
                );
            }

        } catch (IllegalArgumentException
                 | PersistenciaException e) {

            mensajeEstado.setText(e.getMessage());
        }
    }

    // =====================================================
    // BUSCAR CLIENTE
    // =====================================================

    /**
     * Busca un cliente por ID en MySQL y selecciona
     * la fila correspondiente dentro de la tabla.
     */
    @FXML
    protected void buscarCliente() {

        String textoId = campoBuscarId.getText().trim();

        if (textoId.isEmpty()) {
            mensajeEstado.setText(
                    "Debe ingresar el ID del cliente."
            );
            return;
        }

        try {

            int id = Integer.parseInt(textoId);

            if (id <= 0) {
                mensajeEstado.setText(
                        "El ID debe ser mayor que cero."
                );
                return;
            }

            Cliente encontrado =
                    clienteServicio.buscarClientePorId(id);

            if (encontrado == null) {

                mensajeEstado.setText(
                        "No se encontro un cliente con ese ID."
                );
                return;
            }

            // Buscamos la instancia que esta en la tabla.
            Cliente visible = clientes.stream()
                    .filter(c -> c.getId() == id)
                    .findFirst()
                    .orElse(null);

            // Si no aparece, recargamos los registros.
            if (visible == null) {

                actualizarTabla();

                visible = clientes.stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .orElse(null);
            }

            if (visible != null) {

                tablaClientes.getSelectionModel()
                        .select(visible);

                tablaClientes.scrollTo(visible);

                cargarClienteSeleccionado(visible);

            } else {

                cargarClienteSeleccionado(encontrado);
            }

            mensajeEstado.setText(
                    "Cliente encontrado correctamente."
            );

        } catch (NumberFormatException e) {

            mensajeEstado.setText(
                    "El ID debe ser un numero entero."
            );

        } catch (PersistenciaException e) {

            mensajeEstado.setText(e.getMessage());
        }
    }

    // =====================================================
    // ELIMINAR CLIENTE
    // =====================================================

    /**
     * Solicita confirmacion y elimina el cliente
     * seleccionado de la base de datos.
     */
    @FXML
    protected void eliminarCliente() {

        if (clienteSeleccionado == null) {

            mensajeEstado.setText(
                    "Debe seleccionar un cliente para eliminar."
            );
            return;
        }

        int id = clienteSeleccionado.getId();
        String nombre = clienteSeleccionado.getNombre();

        // Confirmacion antes de eliminar.
        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Eliminar cliente");

        confirmacion.setHeaderText(
                "¿Desea eliminar este cliente?"
        );

        confirmacion.setContentText(
                "ID: " + id + "\n"
                        + "Nombre: " + nombre + "\n\n"
                        + "Esta operacion no se puede deshacer."
        );

        confirmacion.initOwner(
                tablaClientes.getScene().getWindow()
        );

        ButtonType respuesta = confirmacion
                .showAndWait()
                .orElse(ButtonType.CANCEL);

        if (respuesta != ButtonType.OK) {
            mensajeEstado.setText(
                    "Eliminacion cancelada."
            );
            return;
        }

        try {

            boolean eliminado =
                    clienteServicio.eliminarCliente(id);

            if (eliminado) {

                actualizarTabla();
                limpiarFormulario();

                mensajeEstado.setText(
                        "Cliente eliminado correctamente."
                );

            } else {

                mensajeEstado.setText(
                        "El cliente ya no existe."
                );
            }

        } catch (PersistenciaException e) {

            // Incluye el caso de cotizaciones asociadas.
            mensajeEstado.setText(e.getMessage());
        }
    }

    // =====================================================
    // CARGA ASINCRONA - CONCURRENCIA
    // =====================================================

    /**
     * Consulta los clientes en un hilo de trabajo.
     * La tabla se actualiza en el hilo de JavaFX
     * para evitar bloquear la interfaz al iniciar.
     */
    private void cargarClientesEnSegundoPlano() {

        mensajeEstado.setText(
                "Cargando clientes desde MySQL..."
        );

        Task<List<Cliente>> tarea = new Task<>() {

            @Override
            protected List<Cliente> call() {
                return clienteServicio.obtenerClientes();
            }
        };

        // Se ejecuta en el hilo de JavaFX.
        tarea.setOnSucceeded(evento -> {

            clientes.setAll(tarea.getValue());

            mensajeEstado.setText(
                    "Clientes cargados: " + clientes.size()
            );
        });

        // Muestra errores sin cerrar la aplicacion.
        tarea.setOnFailed(evento -> {

            Throwable error = tarea.getException();

            if (error instanceof PersistenciaException) {
                mensajeEstado.setText(error.getMessage());
            } else {
                mensajeEstado.setText(
                        "No se pudieron cargar los clientes."
                );
            }
        });

        Thread hilo = new Thread(
                tarea,
                "carga-clientes-mysql"
        );

        hilo.setDaemon(true);
        hilo.start();
    }

    // =====================================================
    // METODOS AUXILIARES
    // =====================================================

    /**
     * Coloca los datos de un cliente en el formulario.
     *
     * @param cliente cliente seleccionado
     */
    private void cargarClienteSeleccionado(
            Cliente cliente
    ) {

        clienteSeleccionado = cliente;

        campoNombre.setText(cliente.getNombre());

        campoEdad.setText(
                String.valueOf(cliente.getEdad())
        );
    }

    /**
     * Consulta nuevamente la base de datos y
     * actualiza los elementos de la tabla.
     */
    private void actualizarTabla() {

        clientes.setAll(
                clienteServicio.obtenerClientes()
        );

        tablaClientes.refresh();
    }

    /**
     * Limpia los campos y la seleccion de la tabla.
     */
    private void limpiarFormulario() {

        tablaClientes.getSelectionModel()
                .clearSelection();

        clienteSeleccionado = null;

        campoNombre.clear();
        campoEdad.clear();
        campoBuscarId.clear();
    }
}
