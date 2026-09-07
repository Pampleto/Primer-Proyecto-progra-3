package modelo;

public class PlanDeportesExtremos extends PlanSeguro {

    public PlanDeportesExtremos() {
        super(
                "Deportes Extremos",
                "Cobertura especial para actividades de mayor riesgo",
                12000.0
        );
    }

    @Override
    public double calcularPrecio(Viaje viaje) {
        double precio = tarifaDiaria * viaje.getCantidadDias();

        if (viaje.getDestino() == TipoDestino.INTERNACIONAL) {
            precio = precio * 2;
        }

        return precio;
    }
}