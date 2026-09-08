package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import modelo.PlanBasico;
import modelo.PlanCoberturaTotal;
import modelo.PlanDeportesExtremos;
import modelo.TipoDestino;
import modelo.Viaje;

/**
 * Controla la vista del cotizador de seguros de viaje.
 *
 * Recibe el destino y la cantidad de dias ingresados por el usuario,
 * construye un Viaje y calcula de forma simultanea el precio de los
 * tres planes de cobertura mostrandolos en tarjetas comparativas.
 */
public class CotizadorController {

    // Selector del tipo de destino (nacional o internacional).
    @FXML
    private ComboBox<TipoDestino> comboTipoDestino;

    // Campo donde se escribe la cantidad de dias del viaje.
    @FXML
    private TextField campoDias;

    // Etiqueta que muestra mensajes informativos al usuario.
    @FXML
    private Label mensajeEstado;

    // Descripcion del plan basico.
    @FXML
    private Label descripcionBasico;

    // Tarifa diaria del plan basico.
    @FXML
    private Label tarifaBasico;

    // Precio final calculado del plan basico.
    @FXML
    private Label precioBasico;

    // Descripcion del plan de cobertura total.
    @FXML
    private Label descripcionTotal;

    // Tarifa diaria del plan de cobertura total.
    @FXML
    private Label tarifaTotal;

    // Precio final calculado del plan de cobertura total.
    @FXML
    private Label precioTotal;

    // Descripcion del plan de deportes extremos.
    @FXML
    private Label descripcionExtremos;

    // Tarifa diaria del plan de deportes extremos.
    @FXML
    private Label tarifaExtremos;

    // Precio final calculado del plan de deportes extremos.
    @FXML
    private Label precioExtremos;

    // Planes de seguro disponibles en el sistema.
    private final PlanBasico planBasico = new PlanBasico();
    private final PlanCoberturaTotal planCoberturaTotal = new PlanCoberturaTotal();
    private final PlanDeportesExtremos planDeportesExtremos = new PlanDeportesExtremos();

    /**
     * Inicializa la vista del cotizador cargando los valores
     * de los planes en las tarjetas y configurando la recotizacion
     * automatica cuando el usuario modifica los dias del viaje.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void initialize() {
        comboTipoDestino.getItems().setAll(TipoDestino.values());
        comboTipoDestino.setValue(TipoDestino.NACIONAL);
        campoDias.setText("7");

        descripcionBasico.setText(planBasico.getDescripcion());
        tarifaBasico.setText(formatearColones(planBasico.getTarifaDiaria()));

        descripcionTotal.setText(planCoberturaTotal.getDescripcion());
        tarifaTotal.setText(formatearColones(planCoberturaTotal.getTarifaDiaria()));

        descripcionExtremos.setText(planDeportesExtremos.getDescripcion());
        tarifaExtremos.setText(formatearColones(planDeportesExtremos.getTarifaDiaria()));

        campoDias.textProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo != null && !nuevo.isBlank()) {
                recalcularCotizacion();
            }
        });

        recalcularCotizacion();
    }

    /**
     * Recalcula la cotizacion cuando el usuario presiona el boton COTIZAR.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void cotizar() {
        recalcularCotizacion();
    }
/**
     * Muestra el mensaje de contratacion para el plan basico.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void contratarBasico() {
        mostrarMensajeContratacion(planBasico.getNombre());
    }

    /**
     * Muestra el mensaje de contratacion para el plan de cobertura total.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void contratarTotal() {
        mostrarMensajeContratacion(planCoberturaTotal.getNombre());
    }

    /**
     * Muestra el mensaje de contratacion para el plan de deportes extremos.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void contratarExtremos() {
        mostrarMensajeContratacion(planDeportesExtremos.getNombre());
    }

    /**
     * Calcula el precio de los tres planes para el viaje ingresado
     * y actualiza las etiquetas de precio de cada tarjeta.
     *
     * Valida que la cantidad de dias sea un numero entero positivo.
     * Si los datos son invalidos muestra un mensaje de error.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void recalcularCotizacion() {
        try {
            int dias = Integer.parseInt(campoDias.getText().trim());
            if (dias <= 0) {
                mostrarPreciosSinDatos();
                mensajeEstado.setText("La cantidad de dias debe ser mayor que cero.");
                return;
            }
            TipoDestino destino = comboTipoDestino.getValue();
            Viaje viaje = new Viaje(destino, dias);

            precioBasico.setText(formatearColones(planBasico.calcularPrecio(viaje)));
            precioTotal.setText(formatearColones(planCoberturaTotal.calcularPrecio(viaje)));
            precioExtremos.setText(formatearColones(planDeportesExtremos.calcularPrecio(viaje)));
            mensajeEstado.setText("Cotizacion actualizada para " + dias + " dias.");
        } catch (NumberFormatException e) {
            mostrarPreciosSinDatos();
            mensajeEstado.setText("La cantidad de dias debe ser un numero entero.");
        }
    }

    /**
     * Muestra las tarjetas de precio sin datos cuando la cotizacion
     * no se puede calcular.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void mostrarPreciosSinDatos() {
        precioBasico.setText("-");
        precioTotal.setText("-");
        precioExtremos.setText("-");
    }

    /**
     * Muestra el mensaje informativo al presionar un boton de contratar.
     *
     * En esta fase del proyecto la contratacion es un esqueleto visual;
     * la asignacion real del plan al cliente se integra en una fase
     * posterior cuando exista la capa de repositorios y servicios.
     *
     * @param nombrePlan nombre del plan que el usuario intenta contratar
     */
    private void mostrarMensajeContratacion(String nombrePlan) {
        mensajeEstado.setText("La contratacion del " + nombrePlan
                + " estara disponible en la siguiente fase del proyecto.");
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