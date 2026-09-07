package modelo;

public class PlanCoberturaTotal extends PlanSeguro {

    public PlanCoberturaTotal() {
        super(
                "Cobertura Total",
                "Cobertura amplia para emergencias, asistencia y gastos medicos",
                7500.0
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