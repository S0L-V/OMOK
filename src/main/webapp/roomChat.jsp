<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Omok Room</title>

<style>
.hidden { display:none; }
.msgArea {
  border:1px solid #ccc; width:420px; height:260px;
  overflow-y:auto; padding:8px;
}
.players { display:flex; gap:16px; margin-top:20px; }
.player { border:1px solid #ccc; padding:10px; border-radius:10px; }
.bubble { display:none; font-size:22px; }
</style>
</head>

<body>

<h2 id="title">대기방</h2>

<!-- 대기방 -->
<div id="waitArea">
  <div class="msgArea"></div>
  <input class="content" placeholder="메시지 입력">
  <button id="sendChatBtn">전송</button>
</div>

<!-- 게임 -->
<div id="gameArea" class="hidden">
  <div class="players">
    <div class="player" data-user="Player1">
      Player1 <span class="bubble"></span>
    </div>
    <div class="player" data-user="Player2">
      Player2 <span class="bubble"></span>
    </div>
  </div>

  <button data-emoji="smile">🙂</button>
  <button data-emoji="angry">😡</button>
  <button data-emoji="clap">👏</button>
</div>

<script>
  window.contextPath = "<%= request.getContextPath() %>";
  const params = new URLSearchParams(location.search);
  window.roomId = params.get("roomId");
</script>

<script src="<%= request.getContextPath() %>/js/ws-common.js"></script>
<script src="<%= request.getContextPath() %>/js/room.js"></script>

</body>
</html>
