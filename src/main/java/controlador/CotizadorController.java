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
import modelo.Cliente;
import servicio.ClienteServicio;
import servicio.CotizacionServicio;

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

    // Campo donde el usuario indica el ID del cliente que realizará la contratación.
    @FXML
    private TextField campoIdCliente;

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

    // Servicio utilizado para consultar los clientes registrados.
    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    // Servicio utilizado para almacenar las cotizaciones contratadas.
    private final CotizacionServicio cotizacionServicio =
            new CotizacionServicio();

    // Planes disponibles en el sistema en el orden basico, total y extremos.
    private final List<PlanSeguro> planes = cotizadorServicio.obtenerPlanes();

    // Viaje correspondiente a la cotizacion mostrada actualmente.
    private Viaje viajeActual;

    // Precios calculados actualmente para cada plan.
    private Map<String, Double> preciosActuales;

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
            recalcularCotizacion();
        });

        comboTipoDestino.setOnAction(event -> recalcularCotizacion());

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
     * Inicia la contratación del plan básico.
     */
    @FXML
    protected void contratarBasico() {
        contratarPlan(planes.get(0));
    }

    /**
     * Inicia la contratación del plan de cobertura total.
     */
    @FXML
    protected void contratarTotal() {
        contratarPlan(planes.get(1));
    }

    /**
     * Inicia la contratación del plan de deportes extremos.
     */
    @FXML
    protected void contratarExtremos() {
        contratarPlan(planes.get(2));
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
            viajeActual = null;
            preciosActuales = null;

            mostrarPreciosSinDatos();
            mensajeEstado.setText(
                    "La cantidad de dias debe ser un numero entero mayor que cero."
            );
            return;
        }

        int dias = Integer.parseInt(textoDias);

        viajeActual = new Viaje(
                comboTipoDestino.getValue(),
                dias
        );

// CotizadorServicio calcula los tres precios simultaneamente.
        preciosActuales = cotizadorServicio.cotizar(viajeActual);

        precioBasico.setText(formatearPrecio(preciosActuales, planes.get(0)));
        precioTotal.setText(formatearPrecio(preciosActuales, planes.get(1)));
        precioExtremos.setText(formatearPrecio(preciosActuales, planes.get(2)));

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
     * Registra la contratación del plan seleccionado para un cliente.
     *
     * Verifica que el ID ingresado sea válido, que el cliente exista
     * y que actualmente haya una cotización calculada.
     *
     * @param plan plan de seguro que se desea contratar
     */
    private void contratarPlan(PlanSeguro plan) {

        String textoId = campoIdCliente.getText().trim();

        // Verifica que el ID sea un número entero.
        if (!Validador.esEntero(textoId)) {
            mensajeEstado.setText(
                    "El ID del cliente debe ser un numero entero valido."
            );
            return;
        }

        int idCliente = Integer.parseInt(textoId);

        // Los identificadores utilizados por los clientes comienzan en 1.
        if (idCliente <= 0) {
            mensajeEstado.setText(
                    "El ID del cliente debe ser mayor que cero."
            );
            return;
        }

        // Busca al cliente registrado.
        Cliente cliente = clienteServicio.buscarClientePorId(idCliente);

        if (cliente == null) {
            mensajeEstado.setText(
                    "No se encontro un cliente con ese ID."
            );
            return;
        }

        // Comprueba que exista una cotización válida en pantalla.
        if (viajeActual == null || preciosActuales == null) {
            mensajeEstado.setText(
                    "Debe realizar una cotizacion valida antes de contratar."
            );
            return;
        }

        // Obtiene exactamente el precio que actualmente ve el usuario.
        Double precioFinal = preciosActuales.get(plan.getNombre());

        if (precioFinal == null) {
            mensajeEstado.setText(
                    "No se pudo obtener el precio del plan seleccionado."
            );
            return;
        }

        try {
            cotizacionServicio.registrarCotizacion(
                    cliente,
                    viajeActual,
                    plan,
                    precioFinal
            );

            mensajeEstado.setText(
                    plan.getNombre()
                            + " contratado correctamente por "
                            + cliente.getNombre()
                            + "."
            );

        } catch (IllegalArgumentException e) {
            mensajeEstado.setText(e.getMessage());
        }
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