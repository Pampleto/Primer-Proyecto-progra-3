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
 * Permite registrar, buscar, seleccionar y modificar
 * clientes desde la interfaz grafica.
 */
public class ClienteController {

    // Campo donde se escribe el nombre del cliente.
    @FXML
    private TextField campoNombre;

    // Campo donde se escribe la edad del cliente.
    @FXML
    private TextField campoEdad;

    // Campo donde se escribe el ID del cliente que se desea buscar.
    @FXML
    private TextField campoBuscarId;

    // Etiqueta utilizada para mostrar mensajes al usuario.
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

    // Servicio encargado de realizar las operaciones de clientes.
    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    // Lista observable utilizada para mostrar los clientes en la tabla.
    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    // Cliente seleccionado actualmente en la tabla.
    private Cliente clienteSeleccionado;

    /**
     * Inicializa la vista de clientes.
     *
     * Configura las columnas de la tabla, carga los clientes
     * registrados y detecta cuando el usuario selecciona una fila.
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

        tablaClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {

                    if (seleccionado != null) {
                        cargarClienteSeleccionado(seleccionado);
                    }
                });

        actualizarTabla();
    }

    /**
     * Registra un nuevo cliente utilizando los datos
     * ingresados en el formulario.
     *
     * Envia el nombre y la edad a ClienteServicio para
     * validar y almacenar el cliente.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void registrarCliente() {

        String nombre = campoNombre.getText();
        String edad = campoEdad.getText();

        try {

            clienteServicio.registrarCliente(
                    nombre,
                    edad
            );

            actualizarTabla();
            limpiarFormulario();

            mensajeEstado.setText(
                    "Cliente registrado correctamente."
            );

        } catch (IllegalArgumentException e) {

            mensajeEstado.setText(
                    e.getMessage()
            );
        }
    }

    /**
     * Modifica los datos del cliente seleccionado.
     *
     * Utiliza el ID del cliente seleccionado junto con
     * el nombre y la edad escritos en el formulario.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void modificarCliente() {

        if (clienteSeleccionado == null) {

            mensajeEstado.setText(
                    "Debe seleccionar un cliente de la tabla."
            );

            return;
        }

        String nombre = campoNombre.getText();
        String edad = campoEdad.getText();

        try {

            boolean modificado =
                    clienteServicio.modificarCliente(
                            clienteSeleccionado.getId(),
                            nombre,
                            edad
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

        } catch (IllegalArgumentException e) {

            mensajeEstado.setText(
                    e.getMessage()
            );
        }
    }

    /**
     * Busca un cliente utilizando el ID ingresado.
     *
     * Obtiene el identificador escrito en el campo de busqueda
     * y solicita a ClienteServicio buscar el cliente correspondiente.
     *
     * No recibe parametros.
     * No retorna ningun valor.
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

            Cliente cliente =
                    clienteServicio.buscarClientePorId(id);

            if (cliente != null) {

                tablaClientes
                        .getSelectionModel()
                        .select(cliente);

                tablaClientes.scrollTo(cliente);

                cargarClienteSeleccionado(cliente);

                mensajeEstado.setText(
                        "Cliente encontrado correctamente."
                );

            } else {

                mensajeEstado.setText(
                        "No se encontro un cliente con ese ID."
                );
            }

        } catch (NumberFormatException e) {

            mensajeEstado.setText(
                    "El ID debe ser un numero entero."
            );
        }
    }

    /**
     * Carga los datos del cliente seleccionado
     * dentro de los campos del formulario.
     *
     * Recibe el cliente seleccionado en la tabla.
     * No retorna ningun valor.
     *
     * @param cliente cliente seleccionado en la tabla
     */
    private void cargarClienteSeleccionado(
            Cliente cliente
    ) {

        clienteSeleccionado = cliente;

        campoNombre.setText(
                cliente.getNombre()
        );

        campoEdad.setText(
                String.valueOf(cliente.getEdad())
        );
    }

    /**
     * Actualiza los clientes mostrados en la tabla.
     *
     * Obtiene todos los clientes registrados desde
     * ClienteServicio y los coloca en la lista observable.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void actualizarTabla() {

        clientes.setAll(
                clienteServicio.obtenerClientes()
        );

        tablaClientes.refresh();
    }

    /**
     * Limpia los campos del formulario y elimina
     * la seleccion actual de la tabla.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void limpiarFormulario() {

        campoNombre.clear();
        campoEdad.clear();
        campoBuscarId.clear();

        clienteSeleccionado = null;

        tablaClientes
                .getSelectionModel()
                .clearSelection();
    }
}