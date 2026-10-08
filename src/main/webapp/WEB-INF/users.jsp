<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="de.hsos.connectfour.model.User" %>
<%
  User currentUser = (User) session.getAttribute("user");
  List<User> users = (List<User>) request.getAttribute("users");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Admin - Users</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/users.css">
</head>
<body>

<div class="page">
  <div class="card glass">

    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="h4 mb-1 text-white">User Management</h2>
        <div class="text-muted small">Ban / unban users</div>
      </div>

      <a class="btn btn-outline-light btn-sm"
         href="<%= request.getContextPath() %>/lobby">
        Back to Lobby
      </a>
    </div>

    <% if (users == null || users.isEmpty()) { %>
    <div class="alert alert-info mb-0">
      No users found.
    </div>
    <% } else { %>

    <div class="table-responsive">
      <table class="table table-dark table-hover align-middle mb-0">
        <thead>
        <tr>
          <th style="width: 90px;">ID</th>
          <th>Username</th>
          <th style="width: 140px;">Role</th>
          <th style="width: 120px;">Banned</th>
          <th style="width: 180px;">Action</th>
        </tr>
        </thead>

        <tbody>
        <% for (User u : users) { %>
        <tr>
          <td><%= u.getId() %></td>

          <td class="fw-semibold"><%= u.getUsername() %></td>

          <td>
            <span class="badge text-bg-secondary"><%= u.getRole() %></span>
          </td>

          <td>
            <% if (u.isBanned()) { %>
            <span class="badge text-bg-danger">YES</span>
            <% } else { %>
            <span class="badge text-bg-success">NO</span>
            <% } %>
          </td>

          <td>
            <% if (currentUser != null && currentUser.getId().equals(u.getId())) { %>
            <span class="text-muted">—</span>
            <% } else { %>
            <form method="post"
                  action="<%= request.getContextPath() %>/admin/users"
                  class="m-0 d-inline">
              <input type="hidden" name="userId" value="<%= u.getId() %>"/>

              <% if (!u.isBanned()) { %>
              <input type="hidden" name="action" value="ban"/>
              <button type="submit"
                      class="btn btn-sm btn-danger"
                      onclick="return confirm('Ban user <%= u.getUsername() %>?');">
                Ban
              </button>
              <% } else { %>
              <input type="hidden" name="action" value="unban"/>
              <button type="submit"
                      class="btn btn-sm btn-success"
                      onclick="return confirm('Unban user <%= u.getUsername() %>?');">
                Unban
              </button>
              <% } %>
            </form>
            <% } %>
          </td>
        </tr>
        <% } %>
        </tbody>
      </table>
    </div>

    <% } %>

  </div>
</div>

<script src="<%= request.getContextPath() %>/js/bootstrap.bundle.js"></script>
</body>
</html>
