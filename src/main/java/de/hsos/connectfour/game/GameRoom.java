package de.hsos.connectfour.game;

import jakarta.websocket.Session;

import java.util.UUID;
/**
 * Repräsentiert einen einzelnen Spielraum.
 *
 * Ein GameRoom verwaltet genau ein Connect-Four-Spiel zwischen
 * einem Host und optional einem Gast. Er speichert:
 * - die zugehörigen Benutzer-IDs
 * - die WebSocket-Sessions der Spieler
 * - den aktuellen Spielstatus
 * - das Spielbrett (ConnectFourBoard)
 *
 * Ein Spielraum durchläuft die Zustände WAITING, RUNNING und FINISHED.
 */
public class GameRoom {

    private final String roomId;
    private final long hostUserId;
    private final String hostUsername;
    private Long guestUserId;
    private Session hostSession;
    private Session guestSession;
    private GameStatus status;
    private final ConnectFourBoard board;

    /**
     * Erstellt einen neuen Spielraum für einen Host.
     *
     * @param hostUserId   ID des Host-Benutzers
     * @param hostUsername Benutzername des Hosts
     * @param hostSession  WebSocket-Session des Hosts
     */
    public GameRoom(long hostUserId,String hostUsername, Session hostSession) {
        this.roomId = UUID.randomUUID().toString();
        this.hostUserId = hostUserId;
        this.hostUsername = hostUsername;
        this.hostSession = hostSession;
        this.status = GameStatus.WAITING;
        this.board = new ConnectFourBoard();
    }

    public String getRoomId() {
        return roomId;
    }

    public long getHostUserId() {
        return hostUserId;
    }

    public Long getGuestUserId() {
        return guestUserId;
    }

    /**
     * Fügt einen Gast dem Spielraum hinzu und setzt den Status auf RUNNING.
     *
     * @param guestUserId  ID des Gast-Benutzers
     * @param guestSession WebSocket-Session des Gasts
     */
    public void setGuestUser(Long guestUserId, Session guestSession) {
        this.guestUserId = guestUserId;
        this.guestSession = guestSession;
        this.status = GameStatus.RUNNING;
    }

    public String getHostUsername() {
        return hostUsername;
    }
    public Session getHostSession() {
        return hostSession;
    }

    public Session getGuestSession() {
        return guestSession;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Prüft, ob der Spielraum noch auf einen zweiten Spieler wartet.
     *
     * @return true, wenn der Status WAITING ist
     */
    public boolean isWaiting() {
        return status == GameStatus.WAITING;
    }

    /**
     * Prüft, ob bereits ein Gast dem Spielraum beigetreten ist.
     *
     * @return true, wenn ein Gast vorhanden ist
     */
    public boolean isFull() {
        return guestUserId != null;
    }

    /**
     * Prüft, ob die übergebene Session zu diesem Spielraum gehört.
     *
     * @param s WebSocket-Session
     * @return true, wenn die Session Host oder Gast ist
     */
    public boolean containsSession(Session s) {
        return (hostSession != null && hostSession.equals(s))
                || (guestSession != null && guestSession.equals(s));
    }

    /**
     * Entfernt eine WebSocket-Session aus dem Spielraum.
     * Wird eine Session entfernt, wird das Spiel aktuell als beendet markiert.
     *
     * @param s zu entfernende WebSocket-Session
     */
    public void removeSession(Session s) {
        if (hostSession != null && hostSession.equals(s)) {
            hostSession = null;
        }
        if (guestSession != null && guestSession.equals(s)) {
            guestSession = null;
        }
        this.status = GameStatus.FINISHED;
    }

    public ConnectFourBoard getBoard() {
        return board;
    }

    /**
     * Prüft, ob die übergebene Session die Host-Session ist.
     */
    public boolean isHostSession(Session s) {
        return hostSession != null && hostSession.equals(s);
    }

    /**
     * Prüft, ob die übergebene Session die Gast-Session ist.
     */
    public boolean isGuestSession(Session s) {
        return guestSession != null && guestSession.equals(s);
    }
}
