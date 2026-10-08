package de.hsos.connectfour.web;

import java.io.*;
import de.hsos.connectfour.auth.AuthHelper;
import de.hsos.connectfour.auth.AuthService;
import de.hsos.connectfour.auth.JwtService;
import de.hsos.connectfour.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * Servlet für die zentrale Lobby des Spiels.
 *
 * Dieses Servlet stellt die Startseite nach erfolgreichem Login dar.
 * Es zeigt benutzerspezifische Informationen an und ermöglicht
 * unter anderem das Ausloggen.
 *
 * URL: /lobby
 */
@WebServlet(name = "Lobby", value = "/lobby")
public class LobbyServlet extends HttpServlet {

    private final JwtService jwtService = new JwtService();
    private final AuthService authService = AuthService.getInstance();

    /**
     * Lädt die Lobby-Seite für authentifizierte Benutzer.
     * Übergibt Benutzername und Admin-Status an die View.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = AuthHelper.requireUser(req, resp, authService, jwtService);
        if (user == null) return;
        req.setAttribute("username", user.getUsername());
        req.setAttribute("isAdmin", user.isAdmin());
        req.getRequestDispatcher("/WEB-INF/lobby.jsp").forward(req, resp);

    }

    /**
     * Verarbeitet Lobby-Aktionen.
     * Unterstützt aktuell das Ausloggen des Benutzers,
     * wobei Session und JWT-Cookie gelöscht werden.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                System.out.println("[LobbyServlet] Logging out user: " + (user != null ? user.getUsername() : "(unknown)"));
                session.invalidate();
            }
            Cookie jwtCookie = new Cookie("jwt", "");
            jwtCookie.setPath(req.getContextPath() + "/");
            jwtCookie.setMaxAge(0); // delete cookie
            resp.addCookie(jwtCookie);
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    /**
     * Wird beim Herunterfahren des Servlets aufgerufen.
     */
    public void destroy() {}
}