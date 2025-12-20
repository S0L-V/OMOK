<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>오목 방</title>

  <link rel="stylesheet" href="/static/room/room.css" />
</head>

<body>
  <main class="wrap" id="room-page"
        data-room-id="<c:out value='${roomId}'/>"
        data-room-name="<c:out value='${roomName}'/>">

    <header class="header">
      <div class="title">
        <h2>🎮 방: <c:out value="${roomName}" /></h2>
        <p class="muted">roomId: <c:out value="${roomId}" /></p>
      </div>

      <div class="actions">
		<form action="${pageContext.request.contextPath}/room/exit" method="post">
		  <input type="hidden" name="roomId" value="${roomId}" />
		  <button type="submit">나가기</button>
		</form>
      </div>
    </header>

    <c:if test="${empty roomId}">
      <section class="card">
        roomId가 없습니다. 올바른 경로로 접근해주세요.
      </section>
    </c:if>

    <section class="grid">
      <section class="card">
        <h3>👥 참가자</h3>
        <ul id="user-list" class="user-list"></ul>
      </section>
      <section class="card">
        <h3>💬 채팅</h3>

        <div id="chat-log" class="chat-log"></div>

        <div class="chat-input-row">
          <input id="chat-input" type="text" placeholder="메시지 입력" />
          <button id="chat-send" type="button">전송</button>
        </div>

        <p id="ws-status" class="muted">WS: 연결 전</p>
      </section>
    </section>
  </main>

  <script src="/static/room/room.js"></script>
</body>
</html>
