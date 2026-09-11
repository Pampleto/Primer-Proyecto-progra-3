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
 * Permite registrar clientes, mostrar los clientes registrados
 * en una tabla, seleccionar un cliente y modificar sus datos.
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

    // Servicio encargado de gestionar los clientes.
    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    // Lista observable utilizada para mostrar los clientes en la tabla.
    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    // Guarda el cliente que el usuario selecciona en la tabla.
    private Cliente clienteSeleccionado;

    /**
     * Inicializa la vista de gestion de clientes.
     *
     * Configura las columnas de la tabla y detecta cuando
     * el usuario selecciona un cliente.
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
     * Envia el nombre y la edad al servicio para validar
     * y almacenar el cliente.
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
     * Utiliza el identificador del cliente seleccionado
     * junto con el nuevo nombre y edad ingresados.
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

        mensajeEstado.setText(
                "Cliente seleccionado: "
                        + cliente.getNombre()
        );
    }

    /**
     * Actualiza los clientes mostrados en la tabla.
     *
     * Obtiene todos los clientes desde ClienteServicio
     * y los coloca dentro de la lista observable.
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

        clienteSeleccionado = null;

        tablaClientes
                .getSelectionModel()
                .clearSelection();
    }
}