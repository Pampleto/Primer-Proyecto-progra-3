package conexion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Administra las conexiones de TravelSafe con MySQL.
 * Las credenciales se obtienen desde un archivo local
 * para evitar almacenarlas directamente en el codigo.
 */
public final class ConexionBD {

    private static final Path ARCHIVO =
            Path.of("db.properties");

    // Evita crear objetos de esta clase.
    private ConexionBD() {
    }

    /**
     * Establece una nueva conexion con la base de datos.
     *
     * @return conexion abierta con MySQL.
     * @throws SQLException si ocurre un error de
     * configuracion o conexion.
     */
    public static Connection getConexion()
            throws SQLException {

        Properties propiedades = new Properties();

        // Comprueba que exista la configuracion.
        if (!Files.isRegularFile(ARCHIVO)) {
            throw new SQLException(
                    "No existe db.properties en la raiz " +
                            "del proyecto."
            );
        }

        // Lee las propiedades de conexion.
        try (InputStream entrada =
                     Files.newInputStream(ARCHIVO)) {

            propiedades.load(entrada);

        } catch (IOException e) {
            throw new SQLException(
                    "No se pudo leer db.properties.", e
            );
        }

        String url = propiedades
                .getProperty("db.url", "").trim();

        String usuario = propiedades
                .getProperty("db.user", "").trim();

        String clave = propiedades
                .getProperty("db.password", "");

        // Verifica que existan los datos necesarios.
        if (url.isEmpty() || usuario.isEmpty()) {
            throw new SQLException(
                    "Falta configurar la URL o " +
                            "el usuario de MySQL."
            );
        }

        // Abre una conexion y la retorna.
        return DriverManager.getConnection(
                url, usuario, clave
        );
    }
}
