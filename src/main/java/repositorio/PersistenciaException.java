package repositorio;

/**
 * Representa un error ocurrido al acceder
 * a la base de datos.
 */
public class PersistenciaException
        extends IllegalStateException {

    /**
     * Construye una excepcion de persistencia.
     *
     * @param mensaje descripcion del error
     * @param causa excepcion original
     */
    public PersistenciaException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}
