package de.hsos.connectfour.websocket;
import de.hsos.connectfour.game.GameManager;
import de.hsos.connectfour.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket-Endpunkt für das Echtzeit-Spiel Connect Four.
 *
 * Der Endpunkt verarbeitet eingehende WebSocket-Verbindungen unter
 * {@code /ws/game}. Über den {@link HttpSessionConfigurator} wird die
 * bestehende HTTP-Session in den WebSocket-Kontext übernommen.
 *
 * Nach erfolgreicher Verbindung werden eingehende Nachrichten an den
 * {@link GameCommandHandler} delegiert.
 *
 * Der Endpunkt verwaltet außerdem das Lifecycle-Handling
 * (OnOpen, OnMessage, OnClose, OnError).
 */
@ServerEndpoint(
        value = "/ws/game",
        configurator = HttpSessionConfigurator.class
)
public class GameEndpoint {
    private static final GameCommandHandler COMMAND_HANDLER = new GameCommandHandler();

    /**
     * Wird beim Öffnen einer neuen WebSocket-Verbindung aufgerufen.
     * Prüft, ob eine gültige HTTP-Session und ein eingeloggter Benutzer
     * vorhanden sind. Bei Erfolg werden Benutzerinformationen in den
     * UserProperties der WebSocket-Session gespeichert.
     *
     * @param wsSession aktuelle WebSocket-Session
     * @param config    Endpoint-Konfiguration mit HTTP-Session
     */
    @OnOpen
    public void onOpen(Session wsSession, EndpointConfig config) {
        HttpSession httpSession =
                (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        System.out.println("GameEndpoint.onOpen: HttpSession = " + httpSession);
        if (httpSession == null) {
            System.out.println("GameEndpoint.onOpen: httpSession is null -> closing");
            closeWithReason(wsSession, "No HTTP session. Please log in first.");
            return;
        }
        User user = (User) httpSession.getAttribute("user");
        System.out.println("GameEndpoint.onOpen: user from HttpSession = "
                + (user != null ? user.getUsername() + " (id=" + user.getId() + ")" : "null"));
        if (user == null) {
            System.out.println("GameEndpoint.onOpen: user is null in HttpSession -> closing");
            closeWithReason(wsSession, "Not logged in. Please log in first.");
            return;
        }
        wsSession.getUserProperties().put("user", user);
        wsSession.getUserProperties().put("userId", user.getId());
        wsSession.getUserProperties().put("username", user.getUsername());
        System.out.println("WebSocket opened for user " + user.getUsername()
                + " (id=" + user.getId() + "), wsSession=" + wsSession.getId());
    }

    /**
     * Wird bei Eingang einer Textnachricht vom Client aufgerufen.
     * Die Nachricht wird an den {@link GameCommandHandler} delegiert.
     *
     * @param message   empfangene Nachricht (Textprotokoll)
     * @param wsSession WebSocket-Session des Clients
     */
    @OnMessage
    public void onMessage(String message, Session wsSession) {
        Object uObj = wsSession.getUserProperties().get("user");
        User user = (User) uObj;
        if (user == null) {
            System.out.println("GameEndpoint.onMessage: user is null in wsSession.getUserProperties()");
            System.out.println("GameEndpoint.onMessage: userProperties keys = " + wsSession.getUserProperties().keySet());
            try {
                wsSession.getBasicRemote().sendText("ERROR:Not logged in.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        System.out.println("WS message from " + user.getUsername()
                + " (" + wsSession.getId() + "): " + message);

        try {
            COMMAND_HANDLER.handle(message.trim(), user, wsSession);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                wsSession.getBasicRemote().sendText("ERROR:Internal error: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Wird beim Schließen der WebSocket-Verbindung aufgerufen.
     * Entfernt die Session aus dem zugehörigen Spielraum.
     *
     * @param wsSession geschlossene WebSocket-Session
     * @param reason    Grund für das Schließen
     */
    @OnClose
    public void onClose(Session wsSession, CloseReason reason) {
        System.out.println("WebSocket closed: " + wsSession.getId()
                + " reason=" + reason);
        GameManager.getInstance().removeSession(wsSession);
    }

    /**
     * Wird bei einem WebSocket-Fehler aufgerufen.
     *
     * @param wsSession betroffene Session (kann null sein)
     * @param throwable aufgetretener Fehler
     */
    @OnError
    public void onError(Session wsSession, Throwable throwable) {
        System.out.println("WebSocket error for session "
                + (wsSession != null ? wsSession.getId() : "null"));
        throwable.printStackTrace();
    }

    /**
     * Schließt eine WebSocket-Session mit einem definierten CloseReason.
     *
     * @param session betroffene Session
     * @param text    Schließgrund
     */
    private void closeWithReason(Session session, String text) {
        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    text
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sendet eine standardisierte Fehlermeldung an den Client.
     *
     * @param session WebSocket-Session
     * @param msg     Fehlermeldung (ohne Prefix)
     */
    private void sendError(Session session, String msg) {
        try {
            session.getBasicRemote().sendText("ERROR:" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
