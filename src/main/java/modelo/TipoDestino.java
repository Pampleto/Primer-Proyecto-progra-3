package modelo;

/**
 * Define los tipos de destino permitidos dentro del sistema.
 *
 * Se utiliza un enum porque un viaje solamente puede tener
 * un destino nacional o internacional.
 */
public enum TipoDestino {

    // Representa un viaje realizado dentro del pais.
    NACIONAL,

    // Representa un viaje realizado fuera del pais.
    INTERNACIONAL
}