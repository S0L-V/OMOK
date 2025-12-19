<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>Lobby Test</title>
  <style>
    body { font-family: Arial, sans-serif; padding: 20px; }
    .box { border: 1px solid #ddd; padding: 16px; border-radius: 8px; max-width: 520px; }
    .ok { color: green; font-weight: bold; }
    .bad { color: red; font-weight: bold; }
    code { background: #f6f8fa; padding: 2px 6px; border-radius: 4px; }
  </style>
</head>
<body>

  <h2>Lobby Test Page</h2>

  <div class="box">
    <%
      String userId = (String) request.getAttribute("loginUserId");
      String nickname = (String) request.getAttribute("loginNickname");

      boolean loggedIn = (userId != null);
    %>

    <p>
      로그인 상태:
      <% if (loggedIn) { %>
        <span class="ok">OK (로그인됨)</span>
      <% } else { %>
        <span class="bad">NO (세션 없음/로그인 정보 없음)</span>
      <% } %>
    </p>

    <p>loginUserId: <code><%= (userId == null ? "null" : userId) %></code></p>
    <p>loginNickname: <code><%= (nickname == null ? "null" : nickname) %></code></p>

    <hr/>

  </div>

</body>
</html>
