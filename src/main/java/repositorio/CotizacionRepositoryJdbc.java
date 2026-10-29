package repositorio;

import conexion.ConexionBD;
import modelo.Cotizacion;
import modelo.Cliente;
import modelo.PlanBasico;
import modelo.PlanCoberturaTotal;
import modelo.PlanDeportesExtremos;
import modelo.PlanSeguro;
import modelo.TipoDestino;
import modelo.Viaje;
import modelo.FiltroCotizaciones;
import modelo.ResumenCotizaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Persistencia de cotizaciones mediante JDBC y MySQL.
 */
public class CotizacionRepositoryJdbc implements CotizacionRepository {

    private final ProveedorConexion proveedorConexion;

    /** Inicializa JDBC con la configuracion de conexion existente. */
    public CotizacionRepositoryJdbc() {
        this(ConexionBD::getConexion);
    }

    /**
     * Permite simular JDBC desde pruebas del mismo paquete sin crear credenciales.
     *
     * @param proveedorConexion fabrica de conexiones que el repositorio cerrara
     */
    CotizacionRepositoryJdbc(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    /** Fabrica interna que conserva las excepciones comprobadas de JDBC. */
    @FunctionalInterface
    interface ProveedorConexion {
        /** @return conexion abierta @throws SQLException si falla la conexion */
        Connection abrir() throws SQLException;
    }

    private static final String CONSULTA = """
            SELECT c.id, c.cliente_id, cl.nombre AS cliente_nombre,
                   cl.edad AS cliente_edad, c.plan_id,
                   p.nombre AS plan_nombre, p.descripcion AS plan_descripcion,
                   p.tarifa_diaria, c.tipo_destino, c.cantidad_dias, c.precio_final
            FROM cotizaciones c
            JOIN clientes cl ON cl.id = c.cliente_id
            JOIN planes_seguro p ON p.id = c.plan_id
            """;

    /**
     * Inserta una cotizacion sin su ID temporal y recupera el ID de MySQL.
     * Requiere un cliente y un plan existentes; no crea registros relacionados.
     *
     * @param cotizacion cotizacion nueva con viaje y precio historico validos
     * @return nueva cotizacion con ID persistente, sin modificar la original
     * @throws IllegalArgumentException si los datos no son validos para el esquema
     * @throws PersistenciaException si falla la insercion o la recuperacion del ID
     */
    @Override
    public Cotizacion guardar(Cotizacion cotizacion) {
        BigDecimal precio = validar(cotizacion);
        int planId = obtenerIdPlan(cotizacion.getPlan());
        double precioFinal = cotizacion.getPrecioFinal();

        String sql = """
                INSERT INTO cotizaciones (
                    cliente_id, plan_id, tipo_destino, cantidad_dias, precio_final
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, cotizacion, planId, precio);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar la cotizacion.");
            }
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    int idGenerado = claves.getInt(1);
                    if (idGenerado <= 0) {
                        throw new SQLException("MySQL devolvio un ID no positivo.");
                    }
                    return new Cotizacion(idGenerado, cotizacion.getCliente(),
                            cotizacion.getViaje(), cotizacion.getPlan(), precioFinal);
                }
            }
            throw new SQLException("MySQL no devolvio el ID generado.");
        } catch (SQLException e) {
            throw error("guardar una cotizacion", e);
        }
    }

    /**
     * Valida los datos compartidos por insercion y actualizacion.
     *
     * @param cotizacion datos que se desean persistir
     * @return precio representable en DECIMAL(12, 2), sin redondeo
     * @throws IllegalArgumentException si los datos son invalidos
     */
    private BigDecimal validar(Cotizacion cotizacion) {
        if (cotizacion == null || cotizacion.getCliente() == null
                || cotizacion.getCliente().getId() <= 0) {
            throw new IllegalArgumentException("Debe indicar un cliente persistido.");
        }
        if (cotizacion.getViaje() == null
                || cotizacion.getViaje().getDestino() == null
                || cotizacion.getViaje().getCantidadDias() <= 0) {
            throw new IllegalArgumentException("El viaje debe tener destino y dias positivos.");
        }
        obtenerIdPlan(cotizacion.getPlan());
        double precioFinal = cotizacion.getPrecioFinal();
        if (!Double.isFinite(precioFinal) || precioFinal <= 0) {
            throw new IllegalArgumentException("El precio final debe ser finito y positivo.");
        }
        BigDecimal precio;
        try {
            precio = BigDecimal.valueOf(precioFinal).setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("El precio final admite como maximo dos decimales.", e);
        }
        if (precio.precision() > 12) {
            throw new IllegalArgumentException("El precio final excede DECIMAL(12, 2).");
        }

        return precio;
    }

    /**
     * Asigna los cinco campos persistentes compartidos por INSERT y UPDATE.
     *
     * @param ps sentencia preparada
     * @param cotizacion datos validados
     * @param planId ID del catalogo
     * @param precio precio decimal validado
     * @throws SQLException si falla la asignacion
     */
    private void asignarDatos(PreparedStatement ps, Cotizacion cotizacion,
                              int planId, BigDecimal precio) throws SQLException {
        ps.setInt(1, cotizacion.getCliente().getId());
        ps.setInt(2, planId);
        ps.setString(3, cotizacion.getViaje().getDestino().name());
        ps.setInt(4, cotizacion.getViaje().getCantidadDias());
        ps.setBigDecimal(5, precio);
    }

    /**
     * Traduce el tipo concreto al ID fijo definido por schema.sql.
     *
     * @param plan plan seleccionado
     * @return ID del catalogo: basico 1, cobertura total 2 o deportes extremos 3
     * @throws IllegalArgumentException si el plan no pertenece al catalogo
     */
    private int obtenerIdPlan(PlanSeguro plan) {
        if (plan != null) {
            if (plan.getClass() == PlanBasico.class) return 1;
            if (plan.getClass() == PlanCoberturaTotal.class) return 2;
            if (plan.getClass() == PlanDeportesExtremos.class) return 3;
        }
        throw new IllegalArgumentException("Debe seleccionar un plan del catalogo.");
    }

    /**
     * Consulta todas las cotizaciones con sus clientes y planes.
     *
     * @return lista ordenada por ID, vacia si no existen cotizaciones
     * @throws PersistenciaException si falla la consulta o el mapeo
     */
    @Override
    public List<Cotizacion> listar() {
        return buscar(FiltroCotizaciones.todos());
    }

    /**
     * Busca una cotizacion por su ID persistente.
     *
     * @param id identificador buscado
     * @return cotizacion encontrada u Optional vacio si no existe
     * @throws PersistenciaException si falla la consulta o el mapeo
     */
    @Override
    public Optional<Cotizacion> buscarPorId(int id) {
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(CONSULTA + " WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("buscar una cotizacion", e);
        }
    }

    /**
     * Actualiza cliente, plan, destino, dias y precio; conserva ID y fecha.
     * No modifica los registros de clientes ni del catalogo de planes.
     *
     * @param cotizacion cotizacion con ID persistente y nuevos datos
     * @return true si MySQL informa una fila afectada, false en caso contrario
     * @throws IllegalArgumentException si el ID o los datos son invalidos
     * @throws PersistenciaException si falla la actualizacion
     */
    @Override
    public boolean actualizar(Cotizacion cotizacion) {
        BigDecimal precio = validar(cotizacion);
        if (cotizacion.getId() <= 0) {
            throw new IllegalArgumentException("Debe indicar un ID persistente para actualizar.");
        }
        String sql = """
                UPDATE cotizaciones
                SET cliente_id = ?, plan_id = ?, tipo_destino = ?,
                    cantidad_dias = ?, precio_final = ?
                WHERE id = ?
                """;
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarDatos(ps, cotizacion, obtenerIdPlan(cotizacion.getPlan()), precio);
            ps.setInt(6, cotizacion.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw error("modificar una cotizacion", e);
        }
    }

    /**
     * Elimina exclusivamente la cotizacion con el ID indicado.
     *
     * @param id identificador a eliminar
     * @return true si se elimino un registro, false si no existe
     * @throws PersistenciaException si falla la eliminacion
     */
    @Override
    public boolean eliminar(int id) {
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(
                     "DELETE FROM cotizaciones WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw error("eliminar una cotizacion", e);
        }
    }

    /**
     * Obtiene la cantidad real de cotizaciones mediante COUNT(*).
     *
     * @return cantidad total de registros
     * @throws PersistenciaException si falla la consulta
     */
    @Override
    public int contar() {
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement("SELECT COUNT(*) FROM cotizaciones");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("MySQL no devolvio el conteo de cotizaciones.");
            }
            return rs.getInt(1);
        } catch (SQLException e) {
            throw error("contar las cotizaciones", e);
        }
    }

    /**
     * Consulta el historial con filtros opcionales parametrizados.
     * Las fechas incluyen dias completos de fecha_contratacion (DATETIME sin fracciones).
     *
     * @param filtros criterios combinados con AND; null equivale a todos
     * @return resultados ordenados por ID, lista vacia si no hay coincidencias
     * @throws PersistenciaException si falla la consulta o el mapeo
     */
    @Override
    public List<Cotizacion> buscar(FiltroCotizaciones filtros) {
        FiltroCotizaciones criterios = filtros == null ? FiltroCotizaciones.todos() : filtros;
        StringBuilder sql = new StringBuilder(CONSULTA).append(" WHERE 1 = 1");
        if (criterios.clienteId() != null) sql.append(" AND c.cliente_id = ?");
        if (criterios.destino() != null) sql.append(" AND c.tipo_destino = ?");
        if (criterios.fechaDesde() != null) sql.append(" AND c.fecha_contratacion >= ?");
        if (criterios.fechaHasta() != null) sql.append(" AND c.fecha_contratacion <= ?");
        sql.append(" ORDER BY c.id");

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            int indice = 1;
            if (criterios.clienteId() != null) ps.setInt(indice++, criterios.clienteId());
            if (criterios.destino() != null) ps.setString(indice++, criterios.destino().name());
            if (criterios.fechaDesde() != null) {
                ps.setTimestamp(indice++, Timestamp.valueOf(criterios.fechaDesde().atStartOfDay()));
            }
            if (criterios.fechaHasta() != null) {
                ps.setTimestamp(indice, Timestamp.valueOf(criterios.fechaHasta().atTime(LocalTime.of(23, 59, 59))));
            }
            List<Cotizacion> resultado = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("buscar en el historial de cotizaciones", e);
        }
    }

    /**
     * Obtiene las agregaciones globales en una sola consulta SQL.
     * El LEFT JOIN incluye planes sin cotizaciones; SUM usa solo precios historicos.
     *
     * @return total, suma decimal y cantidades por ID de plan
     * @throws PersistenciaException si falla la consulta
     */
    @Override
    public ResumenCotizaciones obtenerResumen() {
        String sql = """
                SELECT p.id AS plan_id, COUNT(c.id) AS cantidad,
                       (SELECT COUNT(*) FROM cotizaciones) AS total,
                       (SELECT COALESCE(SUM(precio_final), 0) FROM cotizaciones) AS suma_total
                FROM planes_seguro p
                LEFT JOIN cotizaciones c ON c.plan_id = p.id
                GROUP BY p.id
                ORDER BY p.id
                """;
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            long total = 0;
            BigDecimal suma = new BigDecimal("0.00");
            Map<Integer, Long> cantidades = new LinkedHashMap<>();
            while (rs.next()) {
                total = rs.getLong("total");
                suma = rs.getBigDecimal("suma_total");
                cantidades.put(rs.getInt("plan_id"), rs.getLong("cantidad"));
            }
            return new ResumenCotizaciones(total, suma, cantidades);
        } catch (SQLException e) {
            throw error("obtener estadisticas de cotizaciones", e);
        }
    }

    /**
     * Reconstruye una cotizacion sin recalcular su precio historico.
     * Recupera los datos del catalogo, incluida la tarifa para futuros calculos.
     *
     * @param rs fila de la consulta con sus JOIN
     * @return cotizacion con ID real, cliente, viaje y plan concreto
     * @throws SQLException si falla la lectura o el catalogo no es compatible
     */
    private Cotizacion mapear(ResultSet rs) throws SQLException {
        PlanSeguro plan = switch (rs.getInt("plan_id")) {
            case 1 -> new PlanBasico();
            case 2 -> new PlanCoberturaTotal();
            case 3 -> new PlanDeportesExtremos();
            default -> throw new SQLException("El plan almacenado no pertenece al catalogo Java.");
        };
        plan.setTarifaDiaria(rs.getBigDecimal("tarifa_diaria").doubleValue());
        plan.setNombre(rs.getString("plan_nombre"));
        plan.setDescripcion(rs.getString("plan_descripcion"));
        try {
            Cliente cliente = new Cliente(rs.getInt("cliente_id"),
                    rs.getString("cliente_nombre"), rs.getInt("cliente_edad"));
            Viaje viaje = new Viaje(TipoDestino.valueOf(rs.getString("tipo_destino")),
                    rs.getInt("cantidad_dias"));
            return new Cotizacion(rs.getInt("id"), cliente, viaje, plan,
                    rs.getBigDecimal("precio_final").doubleValue());
        } catch (IllegalArgumentException e) {
            throw new SQLException("La cotizacion almacenada no es compatible con el modelo.", e);
        }
    }

    /**
     * Construye un error de persistencia conservando la causa JDBC.
     *
     * @param operacion operacion que fallo
     * @param causa error original
     * @return excepcion con contexto de la operacion
     */
    private PersistenciaException error(String operacion, SQLException causa) {
        return new PersistenciaException("No fue posible " + operacion
                + " en MySQL. Revise la conexion y la base de datos.", causa);
    }
}
