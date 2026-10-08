package de.hsos.connectfour.web;

import de.hsos.connectfour.auth.AuthHelper;
import de.hsos.connectfour.auth.AuthService;
import de.hsos.connectfour.auth.JwtService;
import de.hsos.connectfour.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet zum Starten eines neuen Spiels als Host.
 *
 * Dieses Servlet prüft zunächst die Authentifizierung des Benutzers.
 * Bei erfolgreicher Prüfung wird die Spielseite im Host-Modus geladen.
 *
 * URL: /host-game
 */
@WebServlet("/host-game")
public class HostGameServlet extends HttpServlet {
    private final JwtService jwtService = new JwtService();
    private final AuthService authService = AuthService.getInstance();

    /**
     * Lädt die Spielseite im HOST-Modus.
     * Der Zugriff ist nur für authentifizierte Benutzer erlaubt.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = AuthHelper.requireUser(req, resp, authService, jwtService);
        if (user == null) return;
        req.setAttribute("mode", "HOST");
        req.getRequestDispatcher("/WEB-INF/game.jsp").forward(req, resp);
    }
}