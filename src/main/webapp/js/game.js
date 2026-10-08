import { wsConnect, wsSendMove, wsClose, wsLeave } from "./ws-client.js";

const { mode, roomId, contextPath } = window.gameConfig || {};

wsConnect({ mode, roomId, contextPath });

document.querySelectorAll(".col-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        const col = parseInt(btn.dataset.col, 10);
        if (Number.isNaN(col)) return;
        wsSendMove(col);
    });
});

const backBtn = document.getElementById("btnBack");
if (backBtn) {
    backBtn.addEventListener("click", (e) => {
        e.preventDefault();
        wsClose();
        window.location.href = contextPath + "/lobby";
    });
}

const leaveBtn = document.getElementById("btnLeave");
if (leaveBtn) {
    leaveBtn.addEventListener("click", () => {
        wsLeave();
    });
}