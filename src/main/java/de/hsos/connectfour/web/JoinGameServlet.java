package de.hsos.connectfour.web;


import de.hsos.connectfour.auth.AuthHelper;
import de.hsos.connectfour.auth.AuthService;
import de.hsos.connectfour.auth.JwtService;
import de.hsos.connectfour.game.GameManager;
import de.hsos.connectfour.game.GameRoom;
import de.hsos.connectfour.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

/**
 * Servlet zum Beitreten eines bestehenden Spiels.
 *
 * Dieses Servlet zeigt verfügbare, wartende Spielräume an
 * und ermöglicht es einem Benutzer, einem Raum als Gast beizutreten.
 *
 * URL: /join
 */
@WebServlet("/join")
public class JoinGameServlet extends HttpServlet {
    private final AuthService authService = AuthService.getInstance();
    private final JwtService jwtService = new JwtService();

    /**
     * Zeigt alle wartenden Spielräume an.
     * Der Zugriff ist nur für authentifizierte Benutzer erlaubt.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = AuthHelper.requireUser(req, resp, authService, jwtService);
        if (user == null) return;
        Collection<GameRoom> waitingRooms = GameManager.getInstance().listWaitingRooms();
        req.setAttribute("rooms", waitingRooms);
        req.getRequestDispatcher("/WEB-INF/join.jsp").forward(req, resp);
    }

    /**
     * Verarbeitet die Auswahl eines Spielraums.
     * Setzt den Spielmodus auf GUEST und leitet zur Spielseite weiter.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String roomId = req.getParameter("roomId");
        if (roomId == null || roomId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/join");
            return;
        }
        req.setAttribute("mode", "GUEST");
        req.setAttribute("roomId", roomId);
        req.getRequestDispatcher("/WEB-INF/game.jsp").forward(req, resp);
    }
}