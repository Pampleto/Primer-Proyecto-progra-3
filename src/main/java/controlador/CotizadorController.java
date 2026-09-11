package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import modelo.PlanSeguro;
import modelo.TipoDestino;
import modelo.Viaje;
import servicio.CotizadorServicio;
import util.Validador;

import java.util.List;
import java.util.Map;

/**
 * Controla la vista del cotizador de seguros de viaje.
 *
 * Recibe el destino y la cantidad de dias ingresados por el usuario,
 * construye un Viaje y delega en CotizadorServicio el calculo
 * simultaneo del precio de los tres planes de cobertura, mostrando
 * los resultados en tarjetas comparativas.
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

    // Motor de cotizacion encargado de calcular los precios de los planes.
    private final CotizadorServicio cotizadorServicio = new CotizadorServicio();

    // Planes disponibles en el sistema en el orden basico, total y extremos.
    private final List<PlanSeguro> planes = cotizadorServicio.obtenerPlanes();

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

        descripcionBasico.setText(planes.get(0).getDescripcion());
        tarifaBasico.setText(formatearColones(planes.get(0).getTarifaDiaria()));

        descripcionTotal.setText(planes.get(1).getDescripcion());
        tarifaTotal.setText(formatearColones(planes.get(1).getTarifaDiaria()));

        descripcionExtremos.setText(planes.get(2).getDescripcion());
        tarifaExtremos.setText(formatearColones(planes.get(2).getTarifaDiaria()));

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
        mostrarMensajeContratacion(planes.get(0).getNombre());
    }

    /**
     * Muestra el mensaje de contratacion para el plan de cobertura total.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void contratarTotal() {
        mostrarMensajeContratacion(planes.get(1).getNombre());
    }

    /**
     * Muestra el mensaje de contratacion para el plan de deportes extremos.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    @FXML
    protected void contratarExtremos() {
        mostrarMensajeContratacion(planes.get(2).getNombre());
    }

    /**
     * Calcula el precio de los tres planes para el viaje ingresado
     * y actualiza las etiquetas de precio de cada tarjeta.
     *
     * Solicita a CotizadorServicio el calculo simultaneo de los tres
     * precios para el viaje construido con el destino y los dias.
     * Se invoca cada vez que el usuario modifica la cantidad de dias.
     *
     * Valida que la cantidad de dias sea un numero entero positivo.
     * Si los datos son invalidos muestra un mensaje de error.
     *
     * No recibe parametros.
     * No retorna ningun valor.
     */
    private void recalcularCotizacion() {
        String textoDias = campoDias.getText().trim();

        if (!Validador.esCantidadDiasValida(textoDias)) {
            mostrarPreciosSinDatos();
            mensajeEstado.setText("La cantidad de dias debe ser un numero entero mayor que cero.");
            return;
        }

        int dias = Integer.parseInt(textoDias);
        Viaje viaje = new Viaje(comboTipoDestino.getValue(), dias);

        // CotizadorServicio calcula los tres precios simultaneamente.
        Map<String, Double> precios = cotizadorServicio.cotizar(viaje);

        precioBasico.setText(formatearPrecio(precios, planes.get(0)));
        precioTotal.setText(formatearPrecio(precios, planes.get(1)));
        precioExtremos.setText(formatearPrecio(precios, planes.get(2)));

        mensajeEstado.setText("Cotizacion actualizada para " + dias + " dias.");
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
     * Obtiene el precio formateado de un plan dentro de una cotizacion.
     *
     * @param precios mapa con los precios calculados para cada plan
     * @param plan plan del cual se desea obtener el precio final
     * @return texto del precio formateado o un guion si el precio no existe
     */
    private String formatearPrecio(Map<String, Double> precios, PlanSeguro plan) {
        Double precio = precios.get(plan.getNombre());
        if (precio == null) {
            return "-";
        }
        return formatearColones(precio);
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