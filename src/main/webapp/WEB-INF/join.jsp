<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Collection" %>
<%@ page import="de.hsos.connectfour.game.GameRoom" %>

<%
  Collection<GameRoom> rooms = (Collection<GameRoom>) request.getAttribute("rooms");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Join a Game</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/join.css">
</head>
<body>
<div class="page">
  <div class="card glass">

    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h1 class="h4 mb-1 text-white">Join a Hosted Game</h1>
        <div class="text-muted small">Select a room and join as Guest</div>
      </div>

      <form method="get" action="<%= request.getContextPath() %>/lobby">
        <button type="submit" class="btn btn-outline-light btn-sm">Back</button>
      </form>
    </div>

    <% if (rooms == null || rooms.isEmpty()) { %>
    <div class="alert alert-info mb-0">
      No hosted games available at the moment.
    </div>
    <% } else { %>

    <form method="post" action="<%= request.getContextPath() %>/join">
      <div class="table-responsive">
        <table class="table table-dark table-hover align-middle mb-3">
          <thead>
          <tr>
            <th>Select</th>
            <th>Host</th>
            <th>Room ID</th>
          </tr>
          </thead>
          <tbody>
          <% for (GameRoom room : rooms) { %>
          <tr>
            <td>
              <input class="form-check-input"
                     type="radio"
                     name="roomId"
                     value="<%= room.getRoomId() %>"
                     required>
            </td>
            <td class="fw-semibold">
              <%= room.getHostUsername() %>
            </td>
            <td>
      <span class="badge bg-secondary">
        <%= room.getRoomId() %>
      </span>
            </td>
          </tr>
          <% } %>
          </tbody>
        </table>
      </div>

      <button type="submit" class="btn btn-primary w-100">
        Join Selected Game
      </button>
    </form>

    <% } %>
  </div>
</div>

</body>
</html>
