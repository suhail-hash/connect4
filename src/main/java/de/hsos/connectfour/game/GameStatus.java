package de.hsos.connectfour.game;

/**
 * Repräsentiert den aktuellen Status eines Spielraums.
 *
 * WAITING  – Spiel wartet auf einen zweiten Spieler
 * RUNNING  – Spiel ist aktiv
 * FINISHED – Spiel wurde beendet
 */
public enum GameStatus {
    WAITING,
    RUNNING,
    FINISHED,
}
