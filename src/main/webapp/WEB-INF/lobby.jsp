<%--
  Created by IntelliJ IDEA.
  User: Suhail
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connect-4 Lobby</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/lobby.css">
</head>
<body>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-10 col-lg-6">

            <div class="card shadow-sm">
                <div class="card-body p-4">

                    <h1 class="h3 text-center mb-3">Connect-4 Lobby</h1>
                    <p class="text-center mb-4">
                        Welcome, <strong><%= request.getAttribute("username") %></strong>
                    </p>
                    <div class="d-grid gap-2">
                        <form method="get" action="<%= request.getContextPath() %>/host-game">
                            <button class="btn btn-host btn-lg w-100" type="submit">Host Game</button>
                        </form>

                        <form method="get" action="<%= request.getContextPath() %>/join">
                            <button class="btn btn-join btn-lg w-100" type="submit">Join Game</button>
                        </form>

                        <form method="post" action="<%= request.getContextPath() %>/lobby">
                            <input type="hidden" name="action" value="logout" />
                            <button class="btn btn-logout btn-lg w-100" type="submit">Logout</button>
                        </form>
                    </div>
                    <%
                        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
                        if (isAdmin != null && isAdmin) {
                    %>
                    <hr class="my-4 divider-light"/>

                    <div class="text-center mb-2">
                        <span class="badge bg-danger">Admin</span>
                    </div>

                    <form method="get" action="<%= request.getContextPath() %>/admin/users">
                        <button class="btn btn-admin w-100" type="submit">View Users (Admin)</button>
                    </form>
                    <%
                        }
                    %>

                </div>
            </div>

        </div>
    </div>
</div>
<script src="<%= request.getContextPath() %>/js/bootstrap.bundle.js"></script>
</body>
</html>
</html>
