package de.hsos.connectfour.game;

import de.hsos.connectfour.model.User;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrale Verwaltungskomponente für laufende Spielräume (GameRooms).
 *
 * Der GameManager verwaltet alle aktiven Räume in einer threadsicheren Map
 * und stellt Methoden bereit zum:
 * - Erstellen eines neuen Raums (Host)
 * - Beitreten zu einem wartenden Raum (Gast)
 * - Auflisten wartender Räume für die Join-Übersicht
 * - Entfernen von Sessions/Räumen bei Verbindungsabbruch
 *
 * Die Klasse ist als Singleton umgesetzt, da sie anwendungweit
 * als zentrale Instanz für die Raumverwaltung dient.
 */
public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private GameManager() {
    }

    /**
     * Liefert die Singleton-Instanz des GameManagers.
     *
     * @return zentrale GameManager-Instanz
     */
    public static GameManager getInstance() {
        return INSTANCE;
    }

    /**
     * Liefert den Spielraum zur gegebenen roomId.
     *
     * @param roomId eindeutige Raum-ID
     * @return GameRoom oder null, falls nicht vorhanden
     */
    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * Erstellt einen neuen Spielraum für den Host und registriert ihn intern.
     * Der neue Raum befindet sich initial im Status WAITING.
     *
     * @param host        Host-Benutzer
     * @param hostSession WebSocket-Session des Hosts
     * @return neu erstellter GameRoom
     */
    public synchronized GameRoom createRoom(User host, Session hostSession) {
        GameRoom room = new GameRoom(host.getId(), host.getUsername(), hostSession);
        rooms.put(room.getRoomId(), room);
        return room;
    }

    /**
     * Lässt einen Gast einem wartenden Raum beitreten.
     * Nach erfolgreichem Beitritt wird der Raum auf RUNNING gesetzt.
     *
     * @param roomId       Raum-ID
     * @param guestUserId  Benutzer-ID des Gasts
     * @param guestSession WebSocket-Session des Gasts
     * @return aktualisierter GameRoom
     * @throws IllegalStateException wenn der Raum nicht existiert oder nicht beitretbar ist
     */
    public synchronized GameRoom joinRoom(String roomId, long guestUserId, Session guestSession)
            throws IllegalStateException {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalStateException("Room not found");
        }
        if (!room.isWaiting() || room.isFull()) {
            throw new IllegalStateException("Room is not joinable");
        }
        room.setGuestUser(guestUserId, guestSession);
        return room;
    }

    /**
     * Gibt alle Spielräume zurück, die aktuell auf einen Gast warten.
     *
     * @return Collection wartender GameRooms
     */
    public Collection<GameRoom> listWaitingRooms() {
        List<GameRoom> result = new ArrayList<>();
        for (GameRoom room : rooms.values()) {
            if (room.isWaiting()) {
                result.add(room);
            }
        }
        return result;
    }

    /**
     * Entfernt eine WebSocket-Session aus dem zugehörigen Spielraum.
     * Der Raum wird dabei beendet und aus der Verwaltung entfernt.
     * Der verbleibende Spieler wird (sofern möglich) über den Abbruch informiert.
     *
     * @param wsSession WebSocket-Session, die entfernt werden soll
     */
    public synchronized void removeSession(Session wsSession) {
        if (wsSession == null) return;
        GameRoom roomToUpdate = null;

        for (GameRoom room : rooms.values()) {
            if (room.containsSession(wsSession)) {
                roomToUpdate = room;
                break;
            }
        }
        if (roomToUpdate == null) {
            System.out.println("removeSession: session " + wsSession.getId() + " not found in any room (already removed)");
            return;
        }
        System.out.println("Removing session from room " + roomToUpdate.getRoomId());
        roomToUpdate.removeSession(wsSession);
        if (roomToUpdate.getStatus() == GameStatus.FINISHED) {
            rooms.remove(roomToUpdate.getRoomId());
            return;
        }
        notifyOtherPlayerRoomClosed(roomToUpdate, wsSession);
        rooms.remove(roomToUpdate.getRoomId());
    }

    /**
     * Entfernt einen Spielraum anhand seiner roomId aus der Verwaltung.
     *
     * @param roomId Raum-ID
     */
    public synchronized void removeRoom(String roomId) {
        if (roomId == null) return;

        GameRoom removed = rooms.remove(roomId);

        if (removed != null) {
            System.out.println("Room " + roomId + " removed from GameManager.");
        }
    }

    /**
     * Informiert den jeweils anderen Spieler, dass das Spiel abgebrochen wurde,
     * weil die Gegenstelle die Verbindung verlassen hat.
     *
     * @param room          betroffener Spielraum
     * @param closedSession Session, die geschlossen wurde
     */
    private void notifyOtherPlayerRoomClosed(GameRoom room, Session closedSession) {
        Session other = null;
        if (room.getHostSession() != null && !room.getHostSession().equals(closedSession)) {
            other = room.getHostSession();
        } else if (room.getGuestSession() != null && !room.getGuestSession().equals(closedSession)) {
            other = room.getGuestSession();
        }
        if (other != null && other.isOpen()) {
            try {
                other.getBasicRemote()
                        .sendText("GAME_ABORTED:OPPONENT_LEFT");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
