package de.hsos.connectfour.model;

/**
 * Modellklasse zur Darstellung eines Benutzers.
 *
 * Die Klasse enthält alle benutzerbezogenen Informationen wie:
 * - eindeutige ID
 * - Benutzername
 * - Passwort-Hash und Salt
 * - Rolle (USER oder ADMIN)
 * - Sperrstatus
 */
public class User {
    private Long id;
    private  String username;
    private  String passwordHash;
    private String salt;
    private String role;
    private boolean banned;

    /**
     * Leerer Standardkonstruktor.
     */
    public User() {}

    /**
     * Erstellt einen Benutzer mit Standardrolle USER
     * und ohne Sperrstatus.
     *
     * @param id           Benutzer-ID
     * @param username     Benutzername
     * @param passwordHash gespeicherter Passwort-Hash
     * @param salt         gespeichertes Salt
     */
    public User(Long id, String username, String passwordHash, String salt) {
        this(id, username, passwordHash, salt, "USER", false);
    }

    /**
     * Erstellt einen vollständigen Benutzer.
     *
     * @param id           Benutzer-ID
     * @param username     Benutzername
     * @param passwordHash gespeicherter Passwort-Hash
     * @param salt         gespeichertes Salt
     * @param role         Benutzerrolle (USER oder ADMIN)
     * @param banned       Sperrstatus
     */
    public User(Long id, String username, String passwordHash, String salt, String role, boolean banned) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.banned = banned;
    }

    /**
     * Prüft, ob der Benutzer die Rolle ADMIN besitzt.
     *
     * @return true, wenn Rolle ADMIN ist
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public String getUsername() {
        return username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public Long getId() {
        return id;
    }
    public String getSalt() {
        return salt;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gibt an, ob der Benutzer gesperrt ist.
     *
     * @return true, wenn der Benutzer gesperrt ist
     */
    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "', banned=" + banned + "}";
    }
}