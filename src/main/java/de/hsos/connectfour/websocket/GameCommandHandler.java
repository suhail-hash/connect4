package de.hsos.connectfour.websocket;
import de.hsos.connectfour.auth.AuthService;
import de.hsos.connectfour.game.GameManager;
import de.hsos.connectfour.game.GameRoom;
import de.hsos.connectfour.game.GameStatus;
import de.hsos.connectfour.model.User;
import jakarta.websocket.Session;
import java.io.IOException;

/**
 * Verarbeitet eingehende WebSocket-Kommandos für das Connect-Four-Spiel.
 *
 * Der Handler interpretiert Textnachrichten des Clients (z.B. CREATE_ROOM,
 * JOIN:<roomId>, MOVE:<col>, LEAVE) und führt die entsprechenden Aktionen
 * im {@link GameManager} bzw. im {@link GameRoom} aus.
 *
 * Zusätzlich werden Sicherheits-/Statusprüfungen durchgeführt, z.B.:
 * - Benutzer muss eingeloggt sein
 * - gesperrte Benutzer dürfen nicht hosten/beitreten bzw. weiterspielen
 * - Züge müssen gültig sein (Reihenfolge, Spalte spielbar)
 *
 * Antworten werden über ein einfaches Textprotokoll an Host/Gast gesendet
 * (z.B. ROOM_CREATED, GAME_START, MOVE, GAME_OVER, ERROR).
 */
public class GameCommandHandler {

    private final GameManager gameManager = GameManager.getInstance();
    private final AuthService authService = AuthService.getInstance();

    /**
     * Einstiegspunkt für die Verarbeitung einer Client-Nachricht.
     * Je nach Kommando wird ein Raum erstellt, beigetreten, ein Zug angewendet
     * oder das Spiel verlassen.
     *
     * @param message   empfangene Nachricht (Textprotokoll)
     * @param user      authentifizierter Benutzer (aus Session/HTTP-Session)
     * @param wsSession aktuelle WebSocket-Session
     * @throws IOException bei Sende-/Socket-Fehlern
     */
    public void handle(String message, User user, Session wsSession) throws IOException {
        if (user == null) {
            sendError(wsSession, "Not logged in.");
            return;
        }

        String cmd = message.trim();

        if (cmd.equalsIgnoreCase("CREATE_ROOM")) {
            if (isBanned(user)) {
                sendError(wsSession, "You are banned and cannot host games.");
                return;
            }
            handleCreateRoom(user, wsSession);

        } else if (cmd.startsWith("JOIN:")) {
            if (isBanned(user)) {
                sendError(wsSession, "You are banned and cannot join games.");
                return;
            }
            handleJoin(cmd, user, wsSession);

        } else if (cmd.startsWith("MOVE:")) {
            if (authService.isBanned(user.getId())) {
                wsSession.getBasicRemote().sendText(
                        "BANNED:You have been banned by an administrator and cannot continue the game."
                );
                GameRoom room = requireRoom(wsSession, false);
                if (room != null) {
                    Boolean isHost = resolvePlayer(wsSession, room);
                    Session other = null;
                    if (Boolean.TRUE.equals(isHost)) {
                        other = room.getGuestSession();
                    } else if (Boolean.FALSE.equals(isHost)) {
                        other = room.getHostSession();
                    }

                    if (other != null && other.isOpen()) {
                        other.getBasicRemote().sendText("GAME_ABORTED:OPPONENT_BANNED");
                    }
                }
                return;
            }
            handleMove(message, wsSession);
        }
           else if (cmd.equalsIgnoreCase("LEAVE")) {
            handleLeave(wsSession);
        }   else {
            sendError(wsSession, "Unknown command: " + cmd);
        }
    }

    /**
     * Erstellt einen neuen Spielraum für den Host und sendet die roomId zurück.
     *
     * @param user      Host-Benutzer
     * @param wsSession WebSocket-Session des Hosts
     * @throws IOException bei Sende-Fehlern
     */
    private void handleCreateRoom(User user, Session wsSession) throws IOException {
        GameRoom room = gameManager.createRoom(user, wsSession);
        wsSession.getUserProperties().put("roomId", room.getRoomId());
        wsSession.getBasicRemote().sendText("ROOM_CREATED:" + room.getRoomId());
    }

    /**
     * Lässt einen Benutzer einem bestehenden Raum als Gast beitreten.
     * Bei Erfolg werden Host und Gast über den Spielstart informiert.
     *
     * @param message   Nachricht im Format JOIN:<roomId>
     * @param user      Gast-Benutzer
     * @param wsSession WebSocket-Session des Gasts
     * @throws IOException bei Sende-Fehlern
     */
    private void handleJoin(String message, User user, Session wsSession) throws IOException {
        String roomId = message.substring("JOIN:".length()).trim();
        if (roomId.isEmpty()) {
            sendError(wsSession, "No roomId provided.");
            return;
        }
        try {
            GameRoom room = gameManager.joinRoom(roomId, user.getId(), wsSession);
            wsSession.getUserProperties().put("roomId", room.getRoomId());
            Session hostSession = room.getHostSession();
            Session guestSession = room.getGuestSession();
            if (hostSession != null && hostSession.isOpen()) {
                hostSession.getBasicRemote()
                        .sendText("GAME_START:HOST:" + room.getRoomId());
            }
            if (guestSession != null && guestSession.isOpen()) {
                guestSession.getBasicRemote()
                        .sendText("GAME_START:GUEST:" + room.getRoomId());
            }
        } catch (IllegalStateException e) {
            sendError(wsSession, e.getMessage());
        }
    }

    /**
     * Verarbeitet einen Spielzug (MOVE:<col>).
     * Prüft Zugreihenfolge und Gültigkeit, broadcastet den Zug und prüft
     * anschließend Gewinn oder Unentschieden.
     *
     * @param message   Nachricht im Format MOVE:<col>
     * @param wsSession WebSocket-Session des Spielers
     * @throws IOException bei Sende-Fehlern
     */
    private void handleMove(String message, Session wsSession) throws IOException {
        String colPart = message.substring("MOVE:".length()).trim();
        int col;
        try {
            col = Integer.parseInt(colPart);
        } catch (NumberFormatException e) {
            sendError(wsSession, "Invalid column: " + colPart);
            return;
        }
        GameRoom room = requireRoom(wsSession, true);
        if (room == null) {
            return;
        }
        Boolean forHostObj = resolvePlayer(wsSession, room);

        if (forHostObj == null) {
            return;
        }
        boolean forHost = forHostObj;
        if (!room.getBoard().isColumnPlayable(col)) {
            sendError(wsSession, "Column is full.");
            return;}
        boolean hostTurn = room.getBoard().isHostTurn();
        if (hostTurn != forHost) {
            sendError(wsSession, "It is not your turn.");
            return; }
        int row = room.getBoard().applyMove(forHost, col);
        if (row == -1) {
            sendError(wsSession, "Illegal move (internal check failed");
            return;}
        // DEBUG: print board after successful move
        room.getBoard().debugPrint();
        String playerStr = forHost ? "HOST" : "GUEST";
        String moveMsg = "MOVE:" + playerStr + ":" + row + ":" + col;
        broadcastToRoom(room, moveMsg);
        int playerVal = forHost ? 1 : 2;
        boolean win = room.getBoard().checkWin(row, col, playerVal);
        Session hostSession = room.getHostSession();
        Session guestSession = room.getGuestSession();
        if (win) {
            room.setStatus(GameStatus.FINISHED);
            if (forHost) {
                if (hostSession != null && hostSession.isOpen()) {
                    hostSession.getBasicRemote().sendText("GAME_OVER:YOU_WIN");}
                if (guestSession != null && guestSession.isOpen()) {
                    guestSession.getBasicRemote().sendText("GAME_OVER:YOU_LOSE");}
            } else {
                if (guestSession != null && guestSession.isOpen()) {
                    guestSession.getBasicRemote().sendText("GAME_OVER:YOU_WIN");}
                if (hostSession != null && hostSession.isOpen()) {
                    hostSession.getBasicRemote().sendText("GAME_OVER:YOU_LOSE");}
            }
            System.out.println("Player " + (forHost ? "HOST" : "GUEST")
                    + " won in room " + room.getRoomId());
            return;
        }
        if (room.getBoard().isBoardFull()) {
            room.setStatus(GameStatus.FINISHED);
            if (hostSession != null && hostSession.isOpen()) {
                hostSession.getBasicRemote().sendText("GAME_OVER:DRAW");
            }
            if (guestSession != null && guestSession.isOpen()) {
                guestSession.getBasicRemote().sendText("GAME_OVER:DRAW");
            }

            System.out.println("Game in room " + room.getRoomId() + " ended in a DRAW.");
        }
    }

    /**
     * Beendet das Spiel aus Sicht des aktuellen Spielers.
     * Informiert den anderen Spieler über den Abbruch und entfernt den Raum.
     *
     * @param wsSession WebSocket-Session des Spielers, der das Spiel verlässt
     * @throws IOException bei Sende-/Close-Fehlern
     */
    private void handleLeave(Session wsSession) throws IOException {
        GameRoom room = requireRoom(wsSession, false);
        if (room == null) return;

        Session other = null;

        if (room.isHostSession(wsSession)) {
            other = room.getGuestSession();
        } else if (room.isGuestSession(wsSession)) {
            other = room.getHostSession();
        }

        if (other != null && other.isOpen()) {
            other.getBasicRemote().sendText("GAME_ABORTED:OPPONENT_LEFT");
        }
        gameManager.removeRoom(room.getRoomId());
        wsSession.close();
    }

    /**
     * Ermittelt den aktuellen Spielraum anhand der roomId aus den UserProperties
     * der WebSocket-Session und validiert optional den Status (RUNNING).
     * Bei Fehlern wird eine ERROR-Nachricht an den Client gesendet.
     *
     * @param wsSession      aktuelle WebSocket-Session
     * @param mustBeRunning  true, wenn das Spiel bereits laufen muss
     * @return GameRoom oder null, wenn kein gültiger Raum ermittelt werden konnte
     * @throws IOException bei Sende-Fehlern
     */
    private GameRoom requireRoom(Session wsSession, boolean mustBeRunning) throws IOException {
        String roomId = (String) wsSession.getUserProperties().get("roomId");
        if (roomId == null) {
            sendError(wsSession, "No room associated with this session.");
            return null;
        }
        GameRoom room = gameManager.getRoom(roomId);
        if (room == null) {
            sendError(wsSession, "Room not found.");
            return null;
        }
        if (mustBeRunning && room.getStatus() != GameStatus.RUNNING) {
            sendError(wsSession, "Game is not running.");
            return null;
        }
        return room;
    }

    /**
     * Bestimmt, ob die gegebene WebSocket-Session zum Host oder Gast gehört.
     *
     * @param wsSession WebSocket-Session
     * @param room      Spielraum
     * @return TRUE für Host, FALSE für Gast oder null (wenn die Session nicht zum Raum gehört)
     * @throws IOException bei Sende-Fehlern
     */
    private Boolean resolvePlayer(Session wsSession, GameRoom room) throws IOException {
        if (room.isHostSession(wsSession)) {
            return Boolean.TRUE;
        }
        if (room.isGuestSession(wsSession)) {
            return Boolean.FALSE;
        }
        sendError(wsSession, "This session does not belong to this room.");
        return null;
    }

    /**
     * Sendet eine Textnachricht an beide Spieler, sofern deren Sessions offen sind.
     *
     * @param room    Spielraum
     * @param message Nachricht
     * @throws IOException bei Sende-Fehlern
     */
    private void broadcastToRoom(GameRoom room, String message) throws IOException {
        Session hostSession = room.getHostSession();
        Session guestSession = room.getGuestSession();

        if (hostSession != null && hostSession.isOpen()) {
            hostSession.getBasicRemote().sendText(message);
        }
        if (guestSession != null && guestSession.isOpen()) {
            guestSession.getBasicRemote().sendText(message);
        }
    }

    /**
     * Sendet eine standardisierte Fehlermeldung an den Client.
     *
     * @param session WebSocket-Session
     * @param msg     Fehlermeldung (ohne Prefix)
     * @throws IOException bei Sende-Fehlern
     */
    private void sendError(Session session, String msg) throws IOException {
        session.getBasicRemote().sendText("ERROR:" + msg);
    }

    /**
     * Prüft, ob ein Benutzer gesperrt ist.
     *
     * @param user Benutzer
     * @return true, wenn der Benutzer gesperrt ist (oder ungültig ist)
     */
    private boolean isBanned(User user) {
        if (user == null || user.getId() == null) {
            return true;
        }
        return authService.isBanned(user.getId());
    }
}