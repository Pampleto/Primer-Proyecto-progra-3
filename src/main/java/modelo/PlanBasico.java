package modelo;

public class PlanBasico extends PlanSeguro {

    public PlanBasico() {
        super(
                "Plan Basico",
                "Cobertura basica para emergencias durante el viaje",
                4000.0
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