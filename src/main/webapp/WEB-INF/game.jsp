<%--
  Created by IntelliJ IDEA.
  User: Suhail
  Date: 2/10/2026
  Time: 3:42 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String mode = (String) request.getAttribute("mode");
  if (mode == null) {
    mode = "UNKNOWN";
  }
  String roomId = (String) request.getAttribute("roomId"); // only set for GUEST
  boolean showLog = false; // set true only when debugging
%>

<html>
<head>
  <title>Connect-4 Game</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/game.css">
</head>


<body>
<div class="page">

  <header class="topbar">
    <div>
      <h1 class="title">Connect-4</h1>
      <div class="subtitle">
        <span class="badge">
          <%
            if ("HOST".equals(mode)) out.print("Player 1 (Host)");
            else if ("GUEST".equals(mode)) out.print("Player 2 (Guest)");
            else out.print(mode);
          %>
        </span>
      </div>
    </div>

    <button type="button" id="btnLeave" class="btn btn-outline-secondary">
      Leave Game
    </button>
  </header>

  <section class="info-row">
    <div id="roomInfo" class="info-box">
      <% if ("GUEST".equals(mode) && roomId != null) { %>
      Room ID: <strong><%= roomId %></strong>
      <% } else { %>
      Room ID will appear here once created.
      <% } %>
    </div>

    <div id="status" class="info-box">
      Connecting to game server...
    </div>
  </section>

  <h3 id="turnIndicator" class="turn">Waiting for game to start...</h3>

  <main class="center">
    <div class="game-wrap">
      <div id="moveControls" class="move-controls">
        <% for (int c = 0; c < 7; c++) { %>
        <button type="button" class="col-btn col-arrow" data-col="<%= c %>" aria-label="Drop in column <%= (c+1) %>">▼</button>
        <% } %>
      </div>
      <div id="board" class="board-grid">
        <% for (int r = 0; r < 6; r++) { %>
        <% for (int c = 0; c < 7; c++) { %>
        <div class="cell" id="cell-<%= r %>-<%= c %>"></div>
        <% } %>
        <% } %>
      </div>
    </div>
  </main>

  <% if (showLog) { %>
  <div id="log" class="log"></div>
  <% } %>
</div>

<div id="gameOverModal" class="modal-overlay" style="display:none;">
  <div class="modal">
    <h2 id="modalTitle">Game Over</h2>
    <p id="modalText"></p>

    <button type="button" id="btnModalLobby">Back to Lobby</button>
  </div>
</div>

<script>
  window.gameConfig = {
    mode: "<%= mode %>",
    roomId: "<%= roomId != null ? roomId : "" %>",
    contextPath: "<%= request.getContextPath() %>"
  };
</script>
<script type="module" src="<%= request.getContextPath() %>/js/game.js"></script>
</body>

</html>
