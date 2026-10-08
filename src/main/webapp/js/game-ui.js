export function uiLog(msg) {
    const logEl = document.getElementById("log");
    if (!logEl) return;
    const p = document.createElement("p");
    p.textContent = msg;
    logEl.appendChild(p);
    logEl.scrollTop = logEl.scrollHeight;
}

export function uiSetStatus(text) {
    const statusEl = document.getElementById("status");
    if (statusEl) statusEl.textContent = text;
}

export function uiSetRoomId(roomId) {
    const roomInfoEl = document.getElementById("roomInfo");
    if (roomInfoEl) roomInfoEl.innerHTML = "Room ID: <strong>" + roomId + "</strong>";
}

export function uiSetTurn(turn) {
    const turnEl = document.getElementById("turnIndicator");
    if (!turnEl) return;

    if (turn === "HOST") turnEl.textContent = "Player 1's turn";
    else if (turn === "GUEST") turnEl.textContent = "Player 2's turn";
    else turnEl.textContent = "Waiting for game to start...";
}

export function uiApplyMove(player, row, col) {
    const cell = document.getElementById("cell-" + row + "-" + col);
    if (!cell) return;

    cell.style.background = (player === "HOST") ? "red" : "yellow";
}

export function uiShowGameOverModal(title, text, onBackToLobby) {
    const overlay = document.getElementById("gameOverModal");
    const t = document.getElementById("modalTitle");
    const p = document.getElementById("modalText");
    const btn = document.getElementById("btnModalLobby");
    if (!overlay || !t || !p || !btn) return;
    t.textContent = title;
    p.textContent = text;
    btn.onclick = function () {
        if (typeof onBackToLobby === "function") onBackToLobby();
    };

    overlay.style.display = "flex";
}