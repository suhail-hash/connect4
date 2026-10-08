<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String mode = (String) request.getAttribute("mode");
  if (mode == null) mode = "login";

  String info = (String) request.getAttribute("info");
  String errorLogin = (String) request.getAttribute("errorLogin");
  String errorRegister = (String) request.getAttribute("errorRegister");
%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Connect-4 Authentication</title>

  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/auth.css">
</head>

<body>
<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-12 col-md-8 col-lg-5">
      <div class="card shadow-sm">
        <div class="card-body p-4">

          <h1 class="h3 text-center mb-4">Connect-4</h1>

          <% if (info != null) { %>
          <div class="alert alert-info" role="alert"><%= info %></div>
          <% } %>
          <% if (errorLogin != null) { %>
          <div class="alert alert-danger" role="alert"><%= errorLogin %></div>
          <% } %>
          <% if (errorRegister != null) { %>
          <div class="alert alert-danger" role="alert"><%= errorRegister %></div>
          <% } %>

          <ul class="nav nav-tabs mb-3" id="authTabs" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link <%= "login".equals(mode) ? "active" : "" %>" id="login-tab"
                      data-bs-toggle="tab" data-bs-target="#login-panel" type="button" role="tab">
                Login
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link <%= "register".equals(mode) ? "active" : "" %>" id="register-tab"
                      data-bs-toggle="tab" data-bs-target="#register-panel" type="button" role="tab">
                Register
              </button>
            </li>
          </ul>

          <div class="tab-content">

            <!-- LOGIN -->
            <div class="tab-pane fade <%= "login".equals(mode) ? "show active" : "" %>" id="login-panel" role="tabpanel">
              <form method="post" action="<%= request.getContextPath() %>/auth">
                <input type="hidden" name="action" value="login"/>

                <div class="mb-3">
                  <label class="form-label">Username</label>
                  <input class="form-control" type="text" name="username" required autocomplete="username">
                </div>

                <div class="mb-3">
                  <label class="form-label">Password</label>
                  <input class="form-control" type="password" name="password" required autocomplete="current-password">
                </div>

                <button class="btn btn-primary w-100" type="submit">Login</button>
              </form>
            </div>

            <!-- REGISTER -->
            <div class="tab-pane fade <%= "register".equals(mode) ? "show active" : "" %>" id="register-panel" role="tabpanel">
              <form method="post" action="<%= request.getContextPath() %>/auth">
                <input type="hidden" name="action" value="register"/>

                <div class="mb-3">
                  <label class="form-label">Username</label>
                  <input class="form-control" type="text" name="username" required autocomplete="username">
                </div>

                <div class="mb-3">
                  <label class="form-label">Password</label>
                  <input class="form-control" type="password" name="password" required autocomplete="new-password">
                </div>

                <button class="btn btn-success w-100" type="submit">Create account</button>
              </form>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</div>

<script src="<%= request.getContextPath() %>/js/bootstrap.bundle.js"></script>
<script src="<%= request.getContextPath() %>/js/auth.js"></script>
</body>
</html>
