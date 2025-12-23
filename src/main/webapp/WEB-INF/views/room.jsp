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
        data-room-name="<c:out value='${roomName}'/>"
        data-play-type="<c:out value='${playType}'/>"
        data-host-user-id="<c:out value='${hostUserId}'/>"
        data-user-id="<c:out value='${userId}'/>"
        >

    <header class="header">
      <div class="title">
        <h2>🎮 방: <c:out value="${roomName}" /></h2>
        <p class="muted">roomId: <c:out value="${roomId}" /></p>
		<p class="muted">
		  playType:
		  <c:choose>
		    <c:when test="${playType eq '0'}">개인전</c:when>
		    <c:when test="${playType eq '1'}">팀전</c:when>
		    <c:otherwise>알 수 없음</c:otherwise>
		  </c:choose>
		</p>
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
      <section class="card side-nav">
       	 <div>
       	  <h3 >👥 참가자</h3>
          <ul id="user-list" class="user-list"></ul>
       	 </div>
		<c:if test="${sessionScope.loginUserId eq sessionScope.hostUserId}">
		  <form id="start-form"
		        method="post"
		        action="${pageContext.request.contextPath}/game/start?roomId=${roomId}&playType=${playType}">
		    <button type="submit" id="btn-start" class="btn-start">
		      🎯 시작하기
		    </button>
		  </form>
		</c:if>
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
