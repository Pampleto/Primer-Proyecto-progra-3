package controlador;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import modelo.ResumenCotizaciones;
import repositorio.PersistenciaException;
import servicio.CotizacionServicio;
import util.Dialogos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Controla la vista de estadisticas globales de cotizaciones.
 *
 * Obtiene el resumen agregado desde MySQL (total de contrataciones,
 * monto acumulado y cantidad por plan) de forma asíncrona mediante
 * un Task de JavaFX, de modo que la interfaz no se bloquee durante
 * la consulta a la base de datos.
 */
public class EstadisticasController {

    // Cantidad total de cotizaciones contratadas.
    @FXML
    private Label totalCotizaciones;

    // Monto acumulado de precios finales (BigDecimal).
    @FXML
    private Label sumaPrecios;

    // Contenedor donde se listan las cantidades por plan.
    @FXML
    private VBox contenedorDesglose;

    // Mensaje informativo al usuario.
    @FXML
    private Label mensajeEstadisticas;

    // Servicio que consulta el resumen agregado en MySQL.
    private final CotizacionServicio cotizacionServicio =
            new CotizacionServicio();

    /**
     * Inicializa la vista y dispara la carga asíncrona de estadísticas.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {
        cargarEstadisticasEnSegundoPlano();
    }

    /**
     * Recalcula y actualiza las estadísticas solicitadas por el usuario.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void actualizarEstadisticas() {
        cargarEstadisticasEnSegundoPlano();
    }

    /**
     * Ejecuta la consulta de estadísticas en un hilo de trabajo.
     *
     * El resumen se obtiene invocando a obtenerResumen() del servicio,
     * que a su vez delega en el repositorio JDBC. La actualización de
     * los controles se realiza en el hilo de JavaFX a través del
     * evento setOnSucceeded del Task, evitando bloqueos visuales.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void cargarEstadisticasEnSegundoPlano() {

        mensajeEstadisticas.setText("Calculando estadísticas...");

        Task<ResumenCotizaciones> tarea = new Task<>() {
            @Override
            protected ResumenCotizaciones call() {
                return cotizacionServicio.obtenerResumen();
            }
        };

        // Se ejecuta en el hilo de JavaFX al completarse la tarea.
        tarea.setOnSucceeded(evento -> {
            aplicarResumen(tarea.getValue());
            mensajeEstadisticas.setText("Estadísticas actualizadas.");
        });

        // Captura fallos de MySQL sin cerrar la aplicación.
        tarea.setOnFailed(evento -> {
            Throwable error = tarea.getException();
            manejarErrorConexion(error);
        });

        Thread hilo = new Thread(
                tarea,
                "consulta-estadisticas-mysql"
        );
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Puebla los paneles con el resumen obtenido del servicio,
     * sin recalcular precios ni listar el historial completo.
     *
     * @param resumen total, suma decimal y cantidades por plan
     */
    private void aplicarResumen(ResumenCotizaciones resumen) {

        totalCotizaciones.setText(
                String.valueOf(resumen.totalCotizaciones())
        );

        sumaPrecios.setText(
                formatearColones(resumen.sumaPreciosFinales())
        );

        contenedorDesglose.getChildren().clear();

        Map<Integer, Long> cantidades = resumen.cantidadesPorPlan();

        if (cantidades.isEmpty()) {
            contenedorDesglose.getChildren().add(
                    new Label("Sin datos de planes.")
            );
            return;
        }

        // Itera el desglose por ID de plan del catálogo (1, 2, 3).
        for (Map.Entry<Integer, Long> entrada :
                cantidades.entrySet()) {

            Label fila = new Label(
                    nombrePlanPorId(entrada.getKey())
                            + ": " + entrada.getValue()
            );
            fila.getStyleClass().add("tarjeta-detalle");
            contenedorDesglose.getChildren().add(fila);
        }
    }

    /**
     * Da formato de moneda en colones a un monto decimal,
     * preservando la precisión exacta del BigDecimal del modelo
     * (no se convierte a double para evitar perder precisión).
     *
     * @param monto suma acumulada obtenida de MySQL
     * @return texto con símbolo de colones y dos decimales
     */
    private String formatearColones(BigDecimal monto) {
        BigDecimal valor = monto == null
                ? BigDecimal.ZERO
                : monto;
        return "\u20A1 "
                + String.format(
                        "%,.2f",
                        valor.setScale(2, RoundingMode.HALF_UP)
                );
    }

    /**
     * Traduce el ID del plan del catálogo a su nombre legible,
     * de acuerdo con la definición establecida en schema.sql.
     *
     * @param id identificador del plan (1 básico, 2 total, 3 deportes)
     * @return nombre descriptivo del plan
     */
    private String nombrePlanPorId(int id) {
        return switch (id) {
            case 1 -> "Plan Básico";
            case 2 -> "Cobertura Total";
            case 3 -> "Deportes Extremos";
            default -> "Plan " + id;
        };
    }

    /**
     * Notifica al usuario un error de base de datos durante
     * la consulta de estadísticas, sin interrumpir la aplicación.
     *
     * @param error excepción capturada del hilo de trabajo
     */
    private void manejarErrorConexion(Throwable error) {
        mensajeEstadisticas.setText(
                "No se pudieron cargar las estadísticas."
        );

        String detalle = error instanceof PersistenciaException
                ? error.getMessage()
                : "Error inesperado: " + error.getMessage();

        Dialogos.mostrarError(
                "Error de base de datos",
                "No se pudieron obtener las estadísticas de MySQL.\n"
                        + detalle
        );
    }
}
