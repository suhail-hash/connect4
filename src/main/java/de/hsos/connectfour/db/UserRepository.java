package de.hsos.connectfour.db;

import de.hsos.connectfour.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository für Benutzerzugriffe auf die H2-Datenbank.
 *
 * Diese Klasse kapselt alle SQL-Operationen für die Tabelle {@code users}
 * Beim Erzeugen der Repository-Instanz wird die Tabelle bei Bedarf angelegt.
 */
public class UserRepository {

    /**
     * Erstellt ein neues UserRepository und legt die Tabelle {@code users}
     * an, falls sie noch nicht existiert.
     */
    public UserRepository() {
        createTableIfNotExists();
    }

    /**
     * Erstellt die Tabelle {@code users}, falls sie noch nicht existiert.
     * Wird intern beim Initialisieren des Repositories aufgerufen.
     */
    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id IDENTITY PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                salt VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL DEFAULT 'USER',
                banned BOOLEAN NOT NULL DEFAULT FALSE
            )
            """;

        try (Connection con = DbUtil.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create users table", e);
        }
    }

    /**
     * Speichert einen neuen Benutzer mit Standardrolle USER und ohne Sperre.
     *
     * @param username Benutzername (eindeutig)
     * @param password gehashter Passwortwert
     * @param salt     Salt zur Passwortprüfung
     * @return true bei erfolgreichem Insert, sonst false
     */
    public boolean saveUser(String username, String password, String salt) {
        return saveUser(username, password, salt, "USER", false);
    }

    /**
     * Speichert einen neuen Benutzer in der Datenbank.
     *
     * @param username Benutzername (eindeutig)
     * @param password gehashter Passwortwert
     * @param salt     Salt zur Passwortprüfung
     * @param role     Benutzerrolle (z.B. USER, ADMIN)
     * @param banned   Sperrstatus
     * @return true bei erfolgreichem Insert, sonst false
     */
    public boolean saveUser(String username, String password, String salt, String role, boolean banned) {
        String sql = "INSERT INTO users (username, password, salt, role, banned) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.println("Saving user to DB: " + username);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, salt);
            ps.setString(4, role);
            ps.setBoolean(5, banned);
            ps.executeUpdate();
            System.out.println("[UserRepository] storing hash=" + password);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prüft, ob ein Benutzername bereits existiert.
     *
     * @param username Benutzername
     * @return true, wenn ein Benutzer mit diesem Namen vorhanden ist
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sucht einen Benutzer anhand des Benutzernamens.
     *
     * @param username Benutzername
     * @return User-Objekt oder null, falls nicht gefunden
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, salt, role, banned FROM users WHERE username = ?";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("salt"),
                            rs.getString("role"),
                            rs.getBoolean("banned")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sucht einen Benutzer anhand der ID.
     *
     * @param id Benutzer-ID
     * @return User-Objekt oder null, falls nicht gefunden
     */
    public User findById(long id) {
        String sql = "SELECT id, username, password, salt, role, banned FROM users WHERE id = ?";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("salt"),
                            rs.getString("role"),
                            rs.getBoolean("banned")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Liefert alle Benutzer sortiert nach Benutzername.
     *
     * @return Liste aller Benutzer
     */
    public List<User> findAll() {
        String sql = "SELECT id, username, password, salt, role, banned FROM users ORDER BY username";

        List<User> users = new ArrayList<>();

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(
                        new User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("salt"),
                                rs.getString("role"),
                                rs.getBoolean("banned")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Setzt den Sperrstatus eines Benutzers.
     *
     * @param userId Benutzer-ID
     * @param banned true = sperren, false = entsperren
     */
    public void setBanned(long userId, boolean banned) {
        String sql = "UPDATE users SET banned = ? WHERE id = ?";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, banned);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüft, ob ein Benutzer gesperrt ist.
     *
     * @param userId Benutzer-ID
     * @return true, wenn der Benutzer gesperrt ist
     */
    public boolean isBanned(long userId) {
        String sql = "SELECT banned FROM users WHERE id = ?";

        try (Connection con = DbUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("banned");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
