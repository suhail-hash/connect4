package de.hsos.connectfour.auth;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.hsos.connectfour.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Hilfsklasse zur zentralen Authentifizierungsprüfung in Servlets.
 *
 * Die Klasse stellt mit {@link #requireUser(HttpServletRequest, HttpServletResponse, AuthService, JwtService)}
 * eine zentrale Methode bereit, die:
 * - den eingeloggten Benutzer aus der HTTP-Session liest
 * - falls keine Session vorhanden ist, ein JWT-Cookie als Fallback validiert
 * - die Benutzerdaten aus der Datenbank aktualisiert (Reload)
 * - gesperrte Benutzer auf eine "banned"-Seite weiterleitet
 *
 */
public final class AuthHelper {
    private static final boolean DEBUG_JWT = true;
    private AuthHelper() {}

    /**
     * Leitet zur Login-Seite weiter und liefert null zurück.
     *
     * @param req  HTTP-Anfrage
     * @param resp HTTP-Antwort
     * @return immer null
     * @throws IOException bei Redirect-Fehlern
     */
    private static User redirectToAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/auth");
        return null;
    }

    /**
     * Liest den Benutzer aus der bestehenden HTTP-Session.
     *
     * @param session HTTP-Session
     * @return User oder null, falls nicht vorhanden
     */
    private static User getUserFromSession(HttpSession session) {
        if (session == null) return null;
        Object obj = session.getAttribute("user");
        return (obj instanceof User u) ? u : null;
    }

    /**
     * Versucht, den Benutzer über ein JWT-Cookie zu ermitteln.
     * Das Token wird validiert und die Benutzer-ID daraus ausgelesen,
     * anschließend wird der Benutzer aus der Datenbank geladen.
     *
     * @param req         HTTP-Anfrage (Cookies)
     * @param resp        HTTP-Antwort
     * @param authService AuthService zum Laden des Benutzers
     * @param jwtService  JwtService zur Tokenprüfung
     * @return User oder null, falls Token ungültig/abgelaufen/nicht vorhanden
     * @throws IOException bei Fehlern im Ablauf
     */
    private static User getUserFromJwt(HttpServletRequest req, HttpServletResponse resp,
                                       AuthService authService, JwtService jwtService) throws IOException {
        String token = getCookieValue(req.getCookies(), "jwt");
        if (token == null) return null;
        DecodedJWT jwt = jwtService.verifyToken(token);
        if (jwt == null) {
            if (DEBUG_JWT) {
                System.out.println("[AuthHelper] Invalid or expired JWT");
            }
            return null;
        }
        if (DEBUG_JWT) {
            System.out.println("[AuthHelper] JWT OK: sub=" + jwt.getSubject()
                    + ", uid=" + jwt.getClaim("uid").asLong());
        }
        Long uid = jwt.getClaim("uid").asLong();
        if (uid == null) return null;
        return authService.findById(uid);
    }

    /**
     * Leitet einen gesperrten Benutzer auf die Sperrseite weiter.
     *
     * @param req  HTTP-Anfrage
     * @param resp HTTP-Antwort
     * @param user gesperrter Benutzer
     */
    private static void forwardBanned(HttpServletRequest req, HttpServletResponse resp, User user) {
        try {
            req.setAttribute("username", user.getUsername());
            req.getRequestDispatcher("/WEB-INF/banned.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stellt sicher, dass ein authentifizierter Benutzer vorhanden ist.
     *
     * Falls ein Benutzer in der Session existiert, wird dieser genutzt und
     * anschließend aus der Datenbank aktualisiert. Falls keine Session existiert,
     * wird versucht, den Benutzer über ein JWT-Cookie wiederherzustellen.
     *
     * Gesperrte Benutzer werden auf {@code /WEB-INF/banned.jsp} weitergeleitet.
     * Ist keine gültige Authentifizierung möglich, erfolgt ein Redirect zu {@code /auth}.
     *
     * @param req         HTTP-Anfrage
     * @param resp        HTTP-Antwort
     * @param authService Service für Benutzerzugriffe/Reload
     * @param jwtService  Service zur JWT-Erstellung und -Validierung
     * @return authentifizierter Benutzer oder null (bei Redirect/Forward)
     * @throws IOException wenn der Redirect fehlschlägt
     */
    public static User requireUser(HttpServletRequest req, HttpServletResponse resp,
                                   AuthService authService, JwtService jwtService) throws IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = getUserFromSession(session);
        if (sessionUser == null) {
            if (DEBUG_JWT) System.out.println("[AuthHelper] No session user -> trying JWT fallback");
            User fromToken = getUserFromJwt(req, resp, authService, jwtService);
            if (fromToken == null) return redirectToAuth(req, resp);

            session = req.getSession(true);
            session.setAttribute("user", fromToken);
            sessionUser = fromToken;
            if (DEBUG_JWT) System.out.println("[AuthHelper] Session restored from JWT for user=" + sessionUser.getUsername());
        }
        User freshUser = authService.reloadUser(sessionUser.getUsername());
        if (freshUser == null) {
            session.invalidate();
            return redirectToAuth(req, resp);
        }
        session.setAttribute("user", freshUser);
        if (freshUser.isBanned()) {
            forwardBanned(req, resp, freshUser);
            return null;
        }
        return freshUser;
    }

    /**
     * Sucht den Wert eines Cookies anhand des Namens.
     *
     * @param cookies Cookie-Array aus der Anfrage
     * @param name    Cookie-Name
     * @return Cookie-Wert oder null
     */
    private static String getCookieValue(Cookie[] cookies, String name) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
