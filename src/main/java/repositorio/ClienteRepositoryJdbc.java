package repositorio;

import conexion.ConexionBD;
import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementa las operaciones CRUD de clientes
 * utilizando JDBC y consultas preparadas.
 */
public class ClienteRepositoryJdbc
        implements ClienteRepository {

    /**
     * Inserta un cliente y recupera su ID.
     *
     * @param cliente cliente nuevo
     * @return cliente registrado con ID de MySQL
     */
    @Override
    public Cliente guardar(Cliente cliente) {

        String sql =
                "INSERT INTO clientes (nombre, edad) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setInt(2, cliente.getEdad());

            int filas = ps.executeUpdate();

            if (filas != 1) {
                throw new SQLException(
                        "No se inserto el cliente."
                );
            }

            try (ResultSet llaves = ps.getGeneratedKeys()) {

                if (llaves.next()) {
                    return new Cliente(
                            llaves.getInt(1),
                            cliente.getNombre(),
                            cliente.getEdad()
                    );
                }
            }

            throw new SQLException(
                    "MySQL no devolvio el ID generado."
            );

        } catch (SQLException e) {
            throw error("registrar un cliente", e);
        }
    }

    /**
     * Consulta todos los clientes registrados.
     *
     * @return lista de clientes ordenada por ID
     */
    @Override
    public List<Cliente> listar() {

        String sql =
                "SELECT id, nombre, edad FROM clientes ORDER BY id";

        List<Cliente> resultado = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }

            return resultado;

        } catch (SQLException e) {
            throw error("obtener la lista de clientes", e);
        }
    }

    /**
     * Busca un cliente por identificador.
     *
     * @param id identificador buscado
     * @return cliente encontrado o Optional vacio
     */
    @Override
    public Optional<Cliente> buscarPorId(int id) {

        String sql =
                "SELECT id, nombre, edad FROM clientes WHERE id = ?";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw error("buscar un cliente", e);
        }
    }

    /**
     * Actualiza nombre y edad de un cliente.
     *
     * @param cliente cliente con los datos nuevos
     * @return true si se actualizo correctamente
     */
    @Override
    public boolean actualizar(Cliente cliente) {

        String sql =
                "UPDATE clientes SET nombre = ?, edad = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setInt(2, cliente.getEdad());
            ps.setInt(3, cliente.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw error("modificar un cliente", e);
        }
    }

    /**
     * Elimina un cliente sin cotizaciones asociadas.
     *
     * @param id identificador del cliente
     * @return true si se elimino
     */
    @Override
    public boolean eliminar(int id) {

        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == 1451) {
                throw new PersistenciaException(
                        "No se puede eliminar el cliente porque " +
                                "tiene cotizaciones asociadas.", e
                );
            }

            throw error("eliminar un cliente", e);
        }
    }

    /**
     * Cuenta los clientes existentes.
     *
     * @return cantidad total de clientes
     */
    @Override
    public int contar() {

        String sql = "SELECT COUNT(*) FROM clientes";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            throw error("contar los clientes", e);
        }
    }

    /**
     * Convierte una fila SQL en un objeto Cliente.
     *
     * @param rs resultado de la consulta
     * @return objeto Cliente reconstruido
     * @throws SQLException si falla la lectura
     */
    private Cliente mapear(ResultSet rs)
            throws SQLException {

        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getInt("edad")
        );
    }

    /**
     * Construye una excepcion de persistencia.
     *
     * @param operacion operacion que fallo
     * @param causa error original
     * @return excepcion con mensaje
     */
    private PersistenciaException error(
            String operacion,
            SQLException causa
    ) {

        return new PersistenciaException(
                "No fue posible " + operacion +
                        " en MySQL. Revise la conexion y la base de datos.",
                causa
        );
    }
}
