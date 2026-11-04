
package co.umariana.dao;

import co.umariana.modelo.Estudiante;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );
    }

    // ETAPA 1: Registrar estudiante
    public boolean registrar(Estudiante estudiante) {
        String sql = """
            INSERT INTO estudiante (nombre, correo, semestre)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCorreo());
            ps.setInt(3, estudiante.getSemestre());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            // Incluye el caso de correo duplicado.
            return false;
        }
    }

    // ETAPA 2: Listar estudiantes ordenados por ID
    public List<Estudiante> listar() {
        List<Estudiante> estudiantes = new ArrayList<>();

        String sql = """
            SELECT id, nombre, correo, semestre
            FROM estudiante
            ORDER BY id ASC
            """;

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                estudiantes.add(mapearEstudiante(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al listar estudiantes", e
            );
        }

        return estudiantes;
    }

    // ETAPA 3: Buscar por ID
    public Estudiante buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }

        String sql = """
            SELECT id, nombre, correo, semestre
            FROM estudiante
            WHERE id = ?
            """;

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudiante(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al buscar estudiante", e
            );
        }

        return null;
    }

    // ETAPA 4: Actualizar estudiante
    public boolean actualizar(Estudiante estudiante) {
        String sql = """
            UPDATE estudiante
            SET nombre = ?, correo = ?, semestre = ?
            WHERE id = ?
            """;

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCorreo());
            ps.setInt(3, estudiante.getSemestre());
            ps.setInt(4, estudiante.getId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            // Incluye el caso de correo usado por otro estudiante.
            return false;
        }
    }

    // ETAPA 5: Eliminar estudiante
    public boolean eliminar(int id) {
        String sql = "DELETE FROM estudiante WHERE id = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al eliminar estudiante", e
            );
        }
    }

    // Convierte cada fila de la base de datos en un objeto Estudiante.
    private Estudiante mapearEstudiante(ResultSet rs)
            throws SQLException {

        return new Estudiante(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("correo"),
            rs.getInt("semestre")
        );
    }
}
