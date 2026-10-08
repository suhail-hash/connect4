package de.hsos.connectfour.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hilfsklasse zur Herstellung von Datenbankverbindungen.
 *
 * Die Klasse kapselt die Konfiguration der H2-Datenbank und stellt eine zentrale Methode
 * zum Erhalten einer JDBC-Verbindung bereit.
 */

public class DbUtil {
    // Pfad ueber Umgebungsvariable CONNECT4_DB_PATH, sonst ~/connect4db
    private static final String URL = "jdbc:h2:file:" + System.getenv().getOrDefault(
            "CONNECT4_DB_PATH", System.getProperty("user.home") + "/connect4db");
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * Lädt den H2-Datenbanktreiber beim Initialisieren der Klasse.
     */
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Driver not found", e);
        }
    }

    /**
     * Erstellt eine neue JDBC-Verbindung zur H2-Datenbank.
     *
     * @return aktive Datenbankverbindung
     * @throws SQLException falls keine Verbindung hergestellt werden kann
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
