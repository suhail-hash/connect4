<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Access blocked</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/auth.css">
</head>
<body>

<div class="container d-flex justify-content-center align-items-center" style="min-height: 100vh;">
  <div class="card shadow-lg text-center p-4" style="max-width: 500px; background: rgba(220, 53, 69, 0.9); border: none; color: white;">

    <h2 class="mb-3">Access Blocked</h2>

    <p class="mb-2">
      Hello, <strong><%= request.getAttribute("username") %></strong>
    </p>

    <p class="mb-4">
      Your account has been banned by an administrator.<br>
      You cannot host or join games at the moment.
    </p>

    <a href="<%= request.getContextPath() %>/auth" class="btn btn-light">
      Back to Login
    </a>

  </div>
</div>

<script src="<%= request.getContextPath() %>/js/bootstrap.bundle.js"></script>
</body>
</html>

