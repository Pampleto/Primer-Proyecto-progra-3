package modelo;

import interfaces.Cotizable;

/**
 * Representa la clase base para todos los planes
 * de seguro disponibles en el sistema.
 *
 * Es abstracta porque no se crean objetos directamente
 * de PlanSeguro, sino objetos de sus clases hijas.
 */
public abstract class PlanSeguro implements Cotizable {

    // Nombre que identifica el plan de seguro.
    protected String nombre;

    // Descripcion de la cobertura ofrecida por el plan.
    protected String descripcion;

    // Precio base que se cobra por cada dia de viaje.
    protected double tarifaDiaria;

    /**
     * Inicializa los datos comunes de cualquier plan de seguro.
     *
     * Recibe el nombre, la descripcion y la tarifa diaria del plan.
     * No retorna ningun valor porque es un constructor.
     *
     * @param nombre nombre del plan de seguro
     * @param descripcion descripcion de la cobertura
     * @param tarifaDiaria precio base cobrado por cada dia
     */
    public PlanSeguro(String nombre, String descripcion, double tarifaDiaria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifaDiaria = tarifaDiaria;
    }

    /**
     * Obtiene el nombre del plan de seguro.
     *
     * No recibe parametros.
     * Retorna el nombre actual del plan.
     *
     * @return nombre del plan
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Modifica el nombre del plan de seguro.
     *
     * Recibe como parametro el nuevo nombre.
     * No retorna ningun valor.
     *
     * @param nombre nuevo nombre del plan
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion del plan de seguro.
     *
     * No recibe parametros.
     * Retorna la descripcion actual del plan.
     *
     * @return descripcion del plan
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Modifica la descripcion del plan de seguro.
     *
     * Recibe como parametro la nueva descripcion.
     * No retorna ningun valor.
     *
     * @param descripcion nueva descripcion del plan
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la tarifa diaria utilizada por el plan.
     *
     * No recibe parametros.
     * Retorna la tarifa diaria actual.
     *
     * @return tarifa diaria del plan
     */
    public double getTarifaDiaria() {
        return tarifaDiaria;
    }

    /**
     * Modifica la tarifa diaria utilizada por el plan.
     *
     * Recibe como parametro la nueva tarifa diaria.
     * No retorna ningun valor.
     *
     * @param tarifaDiaria nueva tarifa diaria del plan
     */
    public void setTarifaDiaria(double tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }

    /**
     * Calcula el precio final del seguro para un viaje.
     *
     * Recibe un objeto Viaje con los datos necesarios para la cotizacion.
     * Retorna el precio final calculado por la clase hija.
     *
     * @param viaje viaje que se desea cotizar
     * @return precio final del seguro
     */
    @Override
    public abstract double calcularPrecio(Viaje viaje);
}