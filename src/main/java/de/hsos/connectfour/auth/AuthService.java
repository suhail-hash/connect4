package de.hsos.connectfour.auth;

import de.hsos.connectfour.db.UserRepository;
import de.hsos.connectfour.model.User;

import java.util.List;

/**
 * Zentrale Service-Klasse für Authentifizierung und Benutzerverwaltung.
 *
 * Der AuthService kapselt die Anwendungslogik für:
 * - Login (Passwortprüfung über Salt + Hash)
 * - Registrierung neuer Benutzer
 * - Admin-Funktionen wie Sperren/Entsperren
 * und ist als Singleton umgesetzt.
 */
public class AuthService {
    private static final AuthService INSTANCE = new AuthService();

    private final UserRepository userRepository;

    private AuthService() {
        this.userRepository = new UserRepository();
        ensureDefaultAdmin();
    }

    /**
     * Liefert die Singleton-Instanz des AuthService.
     *
     * @return zentrale AuthService-Instanz
     */
    public static AuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Authentifiziert einen Benutzer anhand von Benutzername und Passwort.
     * Das Passwort wird mit dem gespeicherten Salt gehasht und mit dem
     * gespeicherten Hash verglichen.
     *
     * @param username Benutzername
     * @param password Klartext-Passwort (Eingabe)
     * @return User bei erfolgreicher Authentifizierung, sonst null
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        if (username.isBlank() || password.isBlank()) return null;
        User user = userRepository.findByUsername(username);
        if (user == null) return null;
        String calculatedHash =
                PasswordUtil.hashPassword(password, user.getSalt());
        if (!calculatedHash.equals(user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    /**
     * Registriert einen neuen Benutzer.
     * Es wird ein Salt erzeugt und ein Hashwert gespeichert.
     * Der Benutzername muss eindeutig sein.
     *
     * @param username Benutzername
     * @param password Klartext-Passwort (Eingabe)
     * @return true bei erfolgreicher Registrierung, sonst false
     */
    public boolean register(String username, String password) {
        System.out.println("AuthService.register called for: " + username);
        if (username == null || password == null ) {
            return false;
        }
        if (username.isBlank() || password.isBlank()) {
            return false;
        }
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        return userRepository.saveUser(username, hash, salt);
    }

    /**
     * Prüft, ob ein Benutzer gesperrt ist.
     *
     * @param userId Benutzer-ID
     * @return true, wenn der Benutzer gesperrt ist
     */
    public boolean isBanned(long userId) {
        return userRepository.isBanned(userId);
    }

    /**
     * Setzt den Sperrstatus eines Benutzers.
     *
     * @param userId Benutzer-ID
     * @param banned true = sperren, false = entsperren
     */
    public void setBanned(long userId, boolean banned) {
        userRepository.setBanned(userId, banned);
    }

    /**
     * Liefert alle registrierten Benutzer.
     *
     * @return Liste aller Benutzer
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Sucht einen Benutzer anhand seiner ID.
     *
     * @param id Benutzer-ID
     * @return User oder null, falls nicht vorhanden
     */
    public User findById(long id) {
        return userRepository.findById(id);
    }

    /**
     * Lädt einen Benutzer erneut aus der Datenbank (z.B. um aktualisierte Daten
     * wie Sperrstatus oder Rolle zu erhalten).
     *
     * @param username Benutzername
     * @return aktueller User aus der Datenbank oder null
     */
    public User reloadUser(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Legt einen Standard-Admin-Account an, falls dieser noch nicht existiert.
     * Wird beim Initialisieren des AuthService ausgeführt.
     */
    private void ensureDefaultAdmin() {
        final String adminUsername = "admin";
        final String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin");
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(adminPassword, salt);
        userRepository.saveUser(adminUsername, hash, salt, "ADMIN", false);

        System.out.println("[AuthService] Default admin created: " + adminUsername);
    }
}
