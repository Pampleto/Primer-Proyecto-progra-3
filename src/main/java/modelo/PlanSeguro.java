package modelo;

import interfaces.Cotizable;

public abstract class PlanSeguro implements Cotizable {

    protected String nombre;
    protected String descripcion;
    protected double tarifaDiaria;

    public PlanSeguro(String nombre, String descripcion, double tarifaDiaria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifaDiaria = tarifaDiaria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getTarifaDiaria() {
        return tarifaDiaria;
    }

    public void setTarifaDiaria(double tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }

    @Override
    public abstract double calcularPrecio(Viaje viaje);
}