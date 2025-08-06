package com.blackjack.database;

import com.blackjack.models.Jugador;
import com.blackjack.utils.Logger;
import java.sql.*;

/**
 * Gestor de base de datos SQLite
 * Implementa el patrón Singleton y maneja todas las operaciones de BD
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:blackjack.db";
    private Connection connection;

    private DatabaseManager() {
        conectar();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void conectar() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            Logger.log("Conexión a base de datos establecida");
        } catch (SQLException e) {
            Logger.logError("Error al conectar con la base de datos", e);
        }
    }

    public void inicializarBaseDatos() {
        try {
            Statement stmt = connection.createStatement();

            // Crear tabla de jugadores
            String sql = "CREATE TABLE IF NOT EXISTS jugadores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT UNIQUE NOT NULL," +
                    "saldo REAL NOT NULL," +
                    "partidas_jugadas INTEGER DEFAULT 0," +
                    "partidas_ganadas INTEGER DEFAULT 0," +
                    "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "ultima_conexion TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            stmt.execute(sql);

            // Crear tabla de historial de partidas
            sql = "CREATE TABLE IF NOT EXISTS historial_partidas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "jugador_id INTEGER," +
                    "apuesta REAL," +
                    "resultado TEXT," +
                    "ganancia REAL," +
                    "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (jugador_id) REFERENCES jugadores(id))";

            stmt.execute(sql);

            Logger.log("Base de datos inicializada correctamente");

        } catch (SQLException e) {
            Logger.logError("Error al inicializar la base de datos", e);
        }
    }

    public Jugador obtenerJugador(String nombre) {
        try {
            String sql = "SELECT * FROM jugadores WHERE nombre = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, nombre);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Jugador jugador = new Jugador(
                        rs.getString("nombre"),
                        rs.getDouble("saldo")
                );
                jugador.setId(rs.getLong("id"));

                // Actualizar última conexión
                actualizarUltimaConexion(jugador.getId());

                return jugador;
            }

        } catch (SQLException e) {
            Logger.logError("Error al obtener jugador: " + nombre, e);
        }

        return null;
    }

    public void guardarJugador(Jugador jugador) {
        try {
            String sql = "INSERT INTO jugadores (nombre, saldo) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, jugador.getNombre());
            pstmt.setDouble(2, jugador.getSaldo());

            pstmt.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                jugador.setId(rs.getLong(1));
            }

            Logger.log("Nuevo jugador guardado: " + jugador.getNombre());

        } catch (SQLException e) {
            Logger.logError("Error al guardar jugador", e);
        }
    }

    public void actualizarSaldo(Jugador jugador) {
        try {
            String sql = "UPDATE jugadores SET saldo = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setDouble(1, jugador.getSaldo());
            pstmt.setLong(2, jugador.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            Logger.logError("Error al actualizar saldo", e);
        }
    }

    public void registrarPartida(Long jugadorId, double apuesta, String resultado, double ganancia) {
        try {
            String sql = "INSERT INTO historial_partidas (jugador_id, apuesta, resultado, ganancia) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, jugadorId);
            pstmt.setDouble(2, apuesta);
            pstmt.setString(3, resultado);
            pstmt.setDouble(4, ganancia);

            pstmt.executeUpdate();

            // Actualizar estadísticas del jugador
            actualizarEstadisticas(jugadorId, resultado.equals("GANA"));

        } catch (SQLException e) {
            Logger.logError("Error al registrar partida", e);
        }
    }

    private void actualizarEstadisticas(Long jugadorId, boolean gano) {
        try {
            String sql = "UPDATE jugadores SET partidas_jugadas = partidas_jugadas + 1" +
                    (gano ? ", partidas_ganadas = partidas_ganadas + 1" : "") +
                    " WHERE id = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, jugadorId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            Logger.logError("Error al actualizar estadísticas", e);
        }
    }

    private void actualizarUltimaConexion(Long jugadorId) {
        try {
            String sql = "UPDATE jugadores SET ultima_conexion = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, jugadorId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            Logger.logError("Error al actualizar última conexión", e);
        }
    }

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.log("Conexión a base de datos cerrada");
            }
        } catch (SQLException e) {
            Logger.logError("Error al cerrar conexión", e);
        }
    }
}
