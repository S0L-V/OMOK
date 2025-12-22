<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>오목 방</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/room/room.css" />
</head>
<body>

<c:set var="rid" value="${param.roomId}" />

<div id="room-page"
     data-room-id="${rid}"
     data-room-name="${roomName}">
  <header class="room-header">
    <div class="room-title">
      <h2>방:</h2>
      <div class="room-sub">roomId: <span id="room-id-text">${rid}</span></div>
    </div>

    <div class="room-actions">
      <button id="btn-leave" type="button">나가기</button>
    </div>
  </header>

  <main class="room-main">
    <section class="panel users">
      <h3>👥 참가자</h3>
      <ul id="user-list" class="user-list">
        <li class="muted">로딩 중...</li>
      </ul>
    </section>

    <section class="panel chat">
      <h3>💬 채팅</h3>

      <div id="chat-log" class="chat-log"></div>

      <div class="chat-input-row">
  		<input id="chat-input" type="text" placeholder="메시지 입력" autocomplete="off" />
  		<button id="chat-send" type="button">전송</button>
  	</div>

      <div id="ws-status" class="ws-status">WS: 준비</div>
    </section>
  </main>
</div>

<script src="${pageContext.request.contextPath}/static/room/room.js"></script>
</body>
</html>
