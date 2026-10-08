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
import java.util.List;

/**
 * Servlet zur Verwaltung der Benutzer durch einen Administrator.
 *
 * Dieses Servlet ermöglicht es einem Admin, alle registrierten Benutzer
 * einzusehen sowie Benutzer zu sperren oder zu entsperren.
 * Der Zugriff ist ausschließlich für Benutzer mit der Rolle ADMIN erlaubt.
 *
 * URL: /admin/users
 */
@WebServlet("/admin/users")
public class AdminServlet extends HttpServlet {
    private final AuthService authService = AuthService.getInstance();
    private final JwtService jwtService = new JwtService();

    /**
     * Zeigt die Benutzerübersicht an.
     * Nur Administratoren dürfen auf diese Seite zugreifen.
     * Leitet bei erfolgreicher Authentifizierung zur users.jsp weiter.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User current = AuthHelper.requireUser(req, resp, authService, jwtService);
        if (current == null) return;

        if (!current.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
            return;
        }

        List<User> users = authService.findAllUsers();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/WEB-INF/users.jsp").forward(req, resp);
    }

    /**
     * Verarbeitet Admin-Aktionen wie Sperren und Entsperren von Benutzern.
     * Ein Administrator kann sich selbst nicht sperren.
     * Nach der Aktion erfolgt eine Weiterleitung zurück zur Benutzerübersicht.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User current = AuthHelper.requireUser(req, resp, authService, jwtService);
        if (current == null) return;

        if (!current.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
            return;
        }

        String action = req.getParameter("action");
        String userIdParam = req.getParameter("userId");

        if (action != null && userIdParam != null) {
            long userId = Long.parseLong(userIdParam);

            // prevent self-ban
            if (current.getId() == userId) {
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }

            if ("ban".equals(action)) authService.setBanned(userId, true);
            else if ("unban".equals(action)) authService.setBanned(userId, false);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
