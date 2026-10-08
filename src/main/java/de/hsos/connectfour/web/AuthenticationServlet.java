package de.hsos.connectfour.web;

import de.hsos.connectfour.auth.AuthService;
import de.hsos.connectfour.auth.JwtService;
import de.hsos.connectfour.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet zur Benutzer-Authentifizierung.
 *
 * Dieses Servlet verarbeitet Login- und Registrierungsanfragen.
 * Bei erfolgreichem Login wird eine HTTP-Session erstellt und
 * zusätzlich ein JWT-Token als HttpOnly-Cookie gesetzt.
 *
 * URL: /auth
 */
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

    private final AuthService authService = AuthService.getInstance();
    private final JwtService jwtService = new JwtService();

    /**
     * Zeigt die Authentifizierungsseite an.
     * Standardmäßig wird der Login-Modus geladen.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("mode", "login");
        req.getRequestDispatcher("/WEB-INF/auth.jsp").forward(req, resp);
    }

    /**
     * Verarbeitet Login- oder Registrierungsanfragen
     * abhängig vom übergebenen action-Parameter.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("register".equalsIgnoreCase(action)) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    /**
     * Führt den Login-Vorgang durch.
     * Bei erfolgreicher Authentifizierung wird eine Session erstellt
     * und ein JWT-Token als Cookie gesetzt.
     *
     * @param req  HTTP-Anfrage mit Benutzername und Passwort
     * @param resp HTTP-Antwort für Weiterleitung oder Fehlermeldung
     */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = authService.authenticate(username, password);

        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);

            String token = jwtService.generateToken(user.getUsername(), user.getId());
            System.out.println("[JWT] Generated for " + user.getUsername() + ": " + token);
            jwtService.logTokenInfo(token);
            jakarta.servlet.http.Cookie jwtCookie =
                    new jakarta.servlet.http.Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath(req.getContextPath() + "/");
            resp.addCookie(jwtCookie);
            resp.sendRedirect(req.getContextPath() + "/lobby"); // adjust if needed
        } else {
            req.setAttribute("mode", "login");
            req.setAttribute("errorLogin", "Invalid username or password.");
            req.getRequestDispatcher("/WEB-INF/auth.jsp").forward(req, resp);
        }
    }

    /**
     * Führt die Registrierung eines neuen Benutzers durch.
     * Bei Erfolg wird zum Login-Modus gewechselt,
     * andernfalls wird eine Fehlermeldung angezeigt.
     *
     * @param req  HTTP-Anfrage mit Registrierungsdaten
     * @param resp HTTP-Antwort zur Weiterleitung oder Anzeige von Fehlermeldungen
     */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        System.out.println("REGISTER attempt: " + username);
        boolean success = authService.register(username, password);

        if (success) {
            req.setAttribute("mode", "login");
            req.setAttribute("info", "Registration successful. You can now log in.");
        } else {
            req.setAttribute("mode", "register");
            req.setAttribute("errorRegister", "Registration failed. Username may already exist or input is invalid.");
        }
        req.getRequestDispatcher("/WEB-INF/auth.jsp").forward(req, resp);
    }
}

