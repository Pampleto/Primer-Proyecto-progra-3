package modelo;

public class Viaje {

    private TipoDestino destino;
    private int cantidadDias;

    public Viaje(TipoDestino destino, int cantidadDias) {
        this.destino = destino;
        this.cantidadDias = cantidadDias;
    }

    public TipoDestino getDestino() {
        return destino;
    }

    public void setDestino(TipoDestino destino) {
        this.destino = destino;
    }

    public int getCantidadDias() {
        return cantidadDias;
    }

    public void setCantidadDias(int cantidadDias) {
        this.cantidadDias = cantidadDias;
    }
}