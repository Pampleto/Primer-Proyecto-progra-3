package controlador;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import modelo.Cotizacion;
import servicio.CotizacionServicio;

/**
 * Controla la vista del historial de cotizaciones.
 *
 * Muestra las cotizaciones contratadas almacenadas en el sistema,
 * incluyendo cliente, destino, dias de viaje, plan y precio final.
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

    // Servicio encargado de consultar las cotizaciones registradas.
    private final CotizacionServicio cotizacionServicio =
            new CotizacionServicio();

    // Lista observable utilizada por JavaFX para mostrar los datos.
    private final ObservableList<Cotizacion> cotizaciones =
            FXCollections.observableArrayList();

    /**
     * Inicializa la tabla del historial y carga las cotizaciones existentes.
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

        actualizarHistorial();
    }

    /**
     * Obtiene las cotizaciones almacenadas y actualiza la tabla.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void actualizarHistorial() {

        cotizaciones.setAll(
                cotizacionServicio.obtenerCotizaciones()
        );

        tablaCotizaciones.setItems(cotizaciones);

        if (cotizaciones.isEmpty()) {
            mensajeHistorial.setText(
                    "Aun no hay cotizaciones contratadas."
            );
        } else {
            mensajeHistorial.setText(
                    "Cotizaciones registradas: " + cotizaciones.size()
            );
        }
    }

    /**
     * Da formato de moneda en colones a un monto.
     *
     * @param monto monto que se desea formatear
     * @return monto con simbolo de colones y separadores de miles
     */
    private String formatearColones(double monto) {
        return "\u20A1 " + String.format("%,.0f", monto);
    }
}