package de.hsos.connectfour.auth;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;

/**
 * Service zur Erstellung und Validierung von JSON Web Tokens (JWT).
 *
 * Der JwtService erzeugt signierte Tokens zur Authentifizierung von Benutzern
 * und überprüft deren Gültigkeit (Signatur und Ablaufzeit).
 *
 * Die Tokens enthalten:
 * - den Benutzernamen (Subject)
 * - die Benutzer-ID (Claim "uid")
 * - ein Ausstellungsdatum
 * - ein Ablaufdatum
 */
public class JwtService {
    // Secret ueber Umgebungsvariable JWT_SECRET; sonst zufaellig (Tokens ungueltig nach Neustart)
    private static final String SECRET_KEY = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET")
            : java.util.UUID.randomUUID() + "" + java.util.UUID.randomUUID();
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000;

    /**
     * Erstellt ein signiertes JWT für einen Benutzer.
     *
     * Das Token enthält Benutzername und Benutzer-ID
     * sowie eine definierte Ablaufzeit.
     *
     * @param username Benutzername
     * @param userId   eindeutige Benutzer-ID
     * @return signiertes JWT als String
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + EXPIRATION_MS);

        return JWT.create()
                .withSubject(username)
                .withClaim("uid", userId)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(ALGORITHM);
    }

    /**
     * Überprüft ein JWT auf Gültigkeit und Signatur.
     *
     * Ist das Token ungültig oder abgelaufen,
     * wird null zurückgegeben.
     *
     * @param token JWT-String
     * @return DecodedJWT bei gültigem Token, sonst null
     */
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(ALGORITHM).build().verify(token);
        } catch (JWTVerificationException e) {
            return null; // invalid / expired token
        }
    }

    /**
     * Gibt Informationen über ein JWT zu Debug-Zwecken aus.
     *
     * @param token JWT-String
     */
    public void logTokenInfo(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt == null) {
            System.out.println("[JWT] Invalid or expired token");
            return;
        }
        System.out.println("[JWT] subject=" + jwt.getSubject());
        System.out.println("[JWT] uid=" + jwt.getClaim("uid").asLong());
        System.out.println("[JWT] issuedAt=" + jwt.getIssuedAt());
        System.out.println("[JWT] expiresAt=" + jwt.getExpiresAt());
    }

}