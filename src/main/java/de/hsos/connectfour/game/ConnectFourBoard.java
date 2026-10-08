package de.hsos.connectfour.game;

/**
 * Modelliert das Spielbrett für Connect Four.
 *
 * Die Klasse verwaltet den internen Zustand des 6x7-Spielfelds,
 * die Zugreihenfolge sowie die Spiellogik wie:
 * - Anwenden eines Zuges
 * - Gewinnprüfung
 * - Prüfung auf Unentschieden
 *
 * Die Klasse enthält ausschließlich Spiellogik und ist unabhängig
 * von WebSocket- oder Servlet-Komponenten.
 */
public class ConnectFourBoard {

    public static final int ROWS = 6;
    public static final int COLS = 7;

    // 0 = empty, 1 = host, 2 = guest
    private final int[][] grid;

    // true  = host's turn
    // false = guest's turn
    private boolean hostTurn;

    /**
     * Initialisiert ein leeres Spielbrett.
     * Standardmäßig beginnt der Host.
     */
    public ConnectFourBoard() {
        this.grid = new int[ROWS][COLS];
        this.hostTurn = true; // host starts by default
    }

    /**
     * Gibt eine Kopie des aktuellen Spielfelds zurück.
     * Eine Kopie wird verwendet, um Manipulationen von außen zu verhindern.
     *
     * @return Kopie des aktuellen Spielfelds
     */
    public int[][] getGridCopy() {
        int[][] copy = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(this.grid[r], 0, copy[r], 0, COLS);
        }
        return copy;
    }

    /**
     * Gibt zurück, ob aktuell der Host am Zug ist.
     *
     * @return true, wenn der Host am Zug ist
     */
    public boolean isHostTurn() {
        return hostTurn;
    }

    /**
     * Prüft, ob in der angegebenen Spalte noch ein Spielstein
     * gesetzt werden kann.
     *
     * @param col Spaltenindex
     * @return true, wenn die Spalte gültig und nicht voll ist
     */
    public boolean isColumnPlayable(int col) {
        if (col < 0 || col >= COLS) {
            return false;
        }
        // If top cell is empty, we can still drop a piece into this column
        return grid[0][col] == 0;
    }

    /**
     * Wendet einen Spielzug an.
     *
     * Der Spielstein wird in die unterste freie Position
     * der angegebenen Spalte gesetzt. Nach erfolgreichem Zug
     * wird die Zugreihenfolge gewechselt.
     *
     * @param forHost true, wenn der Host spielt
     * @param col     Zielspalte
     * @return Zeilenindex des gesetzten Steins oder -1 bei ungültigem Zug
     */
    public int applyMove(boolean forHost, int col) {
        // check correct turn
        if (forHost != hostTurn) {
            return -1; // not this player's turn
        }
        if (col < 0 || col >= COLS) {
            return -1; // invalid column
        }
        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == 0) {
                grid[row][col] = forHost ? 1 : 2;
                // switch turn
                hostTurn = !hostTurn;
                return row;
            }
        }
        // column is full
        return -1;
    }

    public void debugPrint() {
        System.out.println("ConnectFourBoard state:");
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < COLS; c++) {
                sb.append(grid[r][c]).append(' ');
            }
            System.out.println(sb);
        }
        System.out.println("Next turn: " + (hostTurn ? "HOST" : "GUEST"));
    }

    /**
     * Prüft, ob der zuletzt gesetzte Spielstein zu einem Gewinn geführt hat.
     * Es werden horizontale, vertikale und diagonale Richtungen geprüft.
     *
     * @param lastRow   Zeile des letzten Zuges
     * @param lastCol   Spalte des letzten Zuges
     * @param playerVal Spielerwert (1 = Host, 2 = Gast)
     * @return true, wenn vier Steine in einer Reihe gefunden wurden
     */
    public boolean checkWin(int lastRow, int lastCol, int playerVal) {
        int[][] directions = {
                {0, 1},  // horizontal
                {1, 0},  // vertical
                {1, 1},  // diagonal down-right / up-left
                {1, -1}  // diagonal up-right / down-left
        };
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];

            int count = 1; // the last placed stone itself

            // forward direction
            count += countDirection(lastRow, lastCol, dRow, dCol, playerVal);
            // backward direction
            count += countDirection(lastRow, lastCol, -dRow, -dCol, playerVal);

            if (count >= 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zählt zusammenhängende Spielsteine in eine bestimmte Richtung.
     *
     * @param row       Startzeile
     * @param col       Startspalte
     * @param dRow      Zeilenrichtung
     * @param dCol      Spaltenrichtung
     * @param playerVal Spielerwert
     * @return Anzahl zusammenhängender Steine in dieser Richtung
     */
    private int countDirection(int row, int col, int dRow, int dCol, int playerVal) {
        int r = row + dRow;
        int c = col + dCol;
        int count = 0;

        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && grid[r][c] == playerVal) {
            count++;
            r += dRow;
            c += dCol;
        }
        return count;
    }

    /**
     * Prüft, ob das Spielfeld vollständig gefüllt ist.
     *
     * @return true, wenn kein freies Feld mehr vorhanden ist
     */
    public boolean isBoardFull() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    return false; // still at least one empty cell
                }
            }
        }
        return true;
    }
}
/**
 Logikquelle: https://www.baeldung.com/java-connect-4-game
 */