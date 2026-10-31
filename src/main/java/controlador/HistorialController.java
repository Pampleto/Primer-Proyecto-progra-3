package controlador;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Cotizacion;
import modelo.FiltroCotizaciones;
import modelo.TipoDestino;
import repositorio.PersistenciaException;
import servicio.CotizacionServicio;
import util.Dialogos;
import util.Validador;

import java.util.List;

/**
 * Controla la vista del historial de cotizaciones.
 *
 * Muestra las cotizaciones contratadas almacenadas en el sistema,
 * incluyendo cliente, destino, dias de viaje, plan y precio final.
 *
 * Permite filtrar el historial por cliente, destino y rango de
 * fechas mediante un objeto FiltroCotizaciones, ejecutando la
 * consulta en un hilo de trabajo para no bloquear la interfaz.
 */
public class HistorialController {

    // Tabla principal del historial.
    @FXML
    private TableView<Cotizacion> tablaCotizaciones;

    // Identificador de la cotizacion.
    @FXML
    private TableColumn<Cotizacion, Integer> columnaId;

    // Nombre del cliente que realizo la contratacion.
    @FXML
    private TableColumn<Cotizacion, String> columnaCliente;

    // Destino del viaje.
    @FXML
    private TableColumn<Cotizacion, String> columnaDestino;

    // Cantidad de dias del viaje.
    @FXML
    private TableColumn<Cotizacion, Integer> columnaDias;

    // Plan contratado.
    @FXML
    private TableColumn<Cotizacion, String> columnaPlan;

    // Precio final de la contratacion.
    @FXML
    private TableColumn<Cotizacion, String> columnaPrecio;

    // Mensaje informativo mostrado al usuario.
    @FXML
    private Label mensajeHistorial;

    // Campo para filtrar por ID de cliente.
    @FXML
    private TextField campoIdCliente;

    // Selector para filtrar por tipo de destino.
    @FXML
    private ComboBox<TipoDestino> comboTipoDestino;

    // Fechas límite del rango de contratación.
    @FXML
    private DatePicker fechaDesde;

    @FXML
    private DatePicker fechaHasta;

    // Servicio encargado de consultar las cotizaciones registradas.
    private final CotizacionServicio cotizacionServicio =
            new CotizacionServicio();

    // Lista observable utilizada por JavaFX para mostrar los datos.
    private final ObservableList<Cotizacion> cotizaciones =
            FXCollections.observableArrayList();

    /**
     * Inicializa la tabla del historial, configura el
     * selector de destino y lanza la carga asíncrona
     * de las cotizaciones existentes.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {

        columnaId.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        datos.getValue().getId()
                )
        );

        columnaCliente.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        datos.getValue().getCliente().getNombre()
                )
        );

        columnaDestino.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        datos.getValue().getViaje().getDestino().toString()
                )
        );

        columnaDias.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        datos.getValue().getViaje().getCantidadDias()
                )
        );

        columnaPlan.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        datos.getValue().getPlan().getNombre()
                )
        );

        columnaPrecio.setCellValueFactory(datos ->
                new ReadOnlyObjectWrapper<>(
                        formatearColones(
                                datos.getValue().getPrecioFinal()
                        )
                )
        );

        tablaCotizaciones.setItems(cotizaciones);

        comboTipoDestino.getItems().setAll(TipoDestino.values());
        comboTipoDestino.setPromptText("Cualquiera");

        // Carga inicial asíncrona desde MySQL.
        cargarCotizacionesEnSegundoPlano(FiltroCotizaciones.todos());
    }

    /**
     * Construye el objeto FiltroCotizaciones a partir de los
     * controles ingresados por el usuario.
     *
     * @return filtros validados para la consulta
     * @throws IllegalArgumentException si los datos ingresados son invalidos
     */
    private FiltroCotizaciones construirFiltro() {
        Integer clienteId = null;
        String textoId = campoIdCliente.getText().trim();

        if (!textoId.isEmpty()) {
            if (!Validador.esEntero(textoId)) {
                throw new IllegalArgumentException(
                        "El ID de cliente debe ser un numero entero valido."
                );
            }
            int id = Integer.parseInt(textoId);
            if (id <= 0) {
                throw new IllegalArgumentException(
                        "El ID de cliente debe ser mayor que cero."
                );
            }
            clienteId = id;
        }

        return new FiltroCotizaciones(
                clienteId,
                comboTipoDestino.getValue(),
                fechaDesde.getValue(),
                fechaHasta.getValue()
        );
    }

    /**
     * Consulta las cotizaciones aplicando los filtros ingresados.
     * La operación se ejecuta en un hilo de trabajo para no
     * bloquear la interfaz gráfica.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void buscar() {
        FiltroCotizaciones filtro;
        try {
            filtro = construirFiltro();
        } catch (IllegalArgumentException e) {
            Dialogos.mostrarAdvertencia("Filtro invalido", e.getMessage());
            mensajeHistorial.setText(e.getMessage());
            return;
        }
        cargarCotizacionesEnSegundoPlano(filtro);
    }

    /**
     * Limpia los filtros y recarga todo el historial.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void limpiarFiltros() {
        campoIdCliente.clear();
        comboTipoDestino.setValue(null);
        fechaDesde.setValue(null);
        fechaHasta.setValue(null);
        cargarCotizacionesEnSegundoPlano(FiltroCotizaciones.todos());
    }

    /**
     * Ejecuta la consulta a MySQL en un hilo de trabajo.
     * La tabla se actualiza en el hilo de JavaFX mediante
     * el evento setOnSucceeded del Task, evitando bloqueos.
     *
     * @param filtros criterios combinados con AND; null para todos
     */
    private void cargarCotizacionesEnSegundoPlano(
            FiltroCotizaciones filtros
    ) {
        mensajeHistorial.setText("Cargando cotizaciones desde MySQL...");

        Task<List<Cotizacion>> tarea = new Task<>() {
            @Override
            protected List<Cotizacion> call() {
                return cotizacionServicio.buscarCotizaciones(filtros);
            }
        };

        // Se ejecuta en el hilo de JavaFX al completarse la tarea.
        tarea.setOnSucceeded(evento -> {
            cotizaciones.setAll(tarea.getValue());
            tablaCotizaciones.setItems(cotizaciones);
            actualizarMensaje();
        });

        // Captura fallos de MySQL sin cerrar la aplicación.
        tarea.setOnFailed(evento -> {
            Throwable error = tarea.getException();
            manejarErrorConexion(error);
        });

        Thread hilo = new Thread(
                tarea,
                "consulta-historial-mysql"
        );
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Muestra un mensaje según la cantidad de resultados obtenidos.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void actualizarMensaje() {
        if (cotizaciones.isEmpty()) {
            mensajeHistorial.setText(
                    "No se encontraron cotizaciones para los filtros ingresados."
            );
        } else {
            mensajeHistorial.setText(
                    "Cotizaciones registradas: " + cotizaciones.size()
            );
        }
    }

    /**
     * Notifica al usuario un error de base de datos durante la consulta.
     *
     * @param error excepcion capturada del hilo de trabajo
     */
    private void manejarErrorConexion(Throwable error) {
        if (error instanceof PersistenciaException) {
            mensajeHistorial.setText(
                    "No se pudieron cargar las cotizaciones. "
                            + error.getMessage()
            );
            Dialogos.mostrarError(
                    "Error de base de datos",
                    "No se pudo consultar el historial en MySQL.\n"
                            + error.getMessage()
            );
        } else {
            mensajeHistorial.setText(
                    "No se pudieron cargar las cotizaciones."
            );
            Dialogos.mostrarError(
                    "Error inesperado",
                    "Ocurrió un error al consultar el historial.\n"
                            + error.getMessage()
            );
        }
    }

    /**
     * Da formato de moneda en colones a un monto numerico.
     *
     * @param monto monto que se desea formatear
     * @return texto del monto con simbolo de colones y separadores de miles
     */
    private String formatearColones(double monto) {
        return "\u20A1 " + String.format("%,.0f", monto);
    }
}