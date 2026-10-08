import { uiLog, uiSetStatus, uiSetRoomId, uiSetTurn, uiApplyMove, uiShowGameOverModal } from "./game-ui.js";

let gameOver = false;
let ws = null;
let contextPath = "";
let mode = "";
let roomId = "";

export function wsConnect(cfg) {
    contextPath = cfg.contextPath;
    mode = cfg.mode;
    roomId = cfg.roomId || "";

    const wsUrl = "ws://" + window.location.host + contextPath + "/ws/game";
    ws = new WebSocket(wsUrl);

    uiSetStatus("Connecting...");
    uiSetTurn(null);

    ws.onopen = function () {
        uiSetStatus("Connected. Mode = " + mode);
        if (mode === "HOST") {
            uiLog("Sending CREATE_ROOM...");
            ws.send("CREATE_ROOM");
        } else if (mode === "GUEST") {
            if (!roomId) {
                uiLog("No room ID provided for guest.");
                uiSetStatus("Error: No room ID to join.");
            } else {
                uiLog("Sending JOIN for room " + roomId + "...");
                ws.send("JOIN:" + roomId);
            }
        }
    };

    ws.onmessage = function (event) {
        const msg = String(event.data || "");
        uiLog("Server: " + msg);

        if (msg.startsWith("BANNED:")) {
            alert(msg.substring("BANNED:".length));
            window.location.href = contextPath + "/lobby";
            return;
        }

        if (msg.startsWith("GAME_ABORTED:OPPONENT_BANNED")) {
            alert("Opponent banned.");
            window.location.href = contextPath + "/lobby";
            return;
        }

        if (msg.startsWith("ROOM_CREATED:")) {
            const id = msg.substring("ROOM_CREATED:".length);
            uiSetRoomId(id);
            uiSetStatus("Waiting for opponent...");
            uiSetTurn(null);
            return;
        }

        if (msg.startsWith("GAME_START:")) {
            const parts = msg.split(":");
            const who = parts[1] || "";
            if (who === "HOST") uiSetStatus("Game started! You are HOST.");
            else if (who === "GUEST") uiSetStatus("Game started! You are GUEST.");
            else uiSetStatus("Game started!");
            uiSetTurn("HOST");
            return;
        }

        if (msg.startsWith("MOVE:")) {
            if (gameOver) return;
            const parts = msg.split(":"); // MOVE:HOST:row:col
            if (parts.length === 4) {
                const player = parts[1];
                const row = parseInt(parts[2], 10);
                const col = parseInt(parts[3], 10);
                if (!Number.isNaN(row) && !Number.isNaN(col)) {
                    uiApplyMove(player, row, col);
                    uiSetTurn(player === "HOST" ? "GUEST" : "HOST");
                }
            }
            return;
        }

        if (msg.startsWith("GAME_ABORTED:OPPONENT_LEFT")) {
            gameOver = true;
            uiSetStatus("Game over: Opponent left");

            uiShowGameOverModal(
                "Opponent left",
                "You win by forfeit.",
                () => {
                    wsClose();
                    window.location.href = contextPath + "/lobby";
                }
            );
            return;
        }

        if (msg.startsWith("GAME_OVER:YOU_WIN")) {
            gameOver = true;
            uiSetStatus("Game over: YOU WIN");

            const youAre = (mode === "HOST") ? "Player 1" : "Player 2";
            uiShowGameOverModal(youAre + " won!", "Congratulations!", backToLobby);
            return;
        }

        if (msg.startsWith("GAME_OVER:YOU_LOSE")) {
            gameOver = true;
            uiSetStatus("Game over: YOU LOSE");

            const winner = (mode === "HOST") ? "Player 2" : "Player 1";
            uiShowGameOverModal( winner + " won!", "Better luck next time!", backToLobby);
            return;
        }

        if (msg.startsWith("ERROR:")) {
            const err = msg.substring("ERROR:".length);
            uiSetStatus("Error: " + err);
            if (mode === "GUEST" && (err.includes("not found") || err.includes("joinable"))) {
                alert("That room is no longer available. Please pick another one.");
                window.location.href = contextPath + "/join";
            }
            return;
        }
    };

    ws.onclose = function (e) {
        uiLog("WebSocket closed: " + e.code + " " + e.reason);
        uiSetStatus("Connection closed.");
    };

    ws.onerror = function () {
        uiLog("WebSocket error.");
        uiSetStatus("WebSocket error.");
    };

    function backToLobby() {
        wsClose();
        window.location.href = contextPath + "/lobby";
    }
}

export function wsSendMove(col) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        uiLog("Cannot send move: WebSocket not open.");
        return;
    }
    ws.send("MOVE:" + col);
}

export function wsClose() {
    if (!ws) return;
    try { ws.close(1000, "Player left to lobby"); } catch (e) {}
}

export function wsLeave() {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        window.location.href = contextPath + "/lobby";
        return;
    }
    ws.send("LEAVE");
    setTimeout(() => {
        ws.close();
        window.location.href = contextPath + "/lobby";
    }, 100);
}