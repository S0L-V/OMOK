<%@ page contentType="text/html; charset=UTF-8" %>

<!-- 게임 중 이모지 전용 UI (fragment) -->
<div class="emoji-game-wrap">
  <div class="player-cards">
    <div id="p1" class="player-card">
      <div class="profile"></div>
      <span class="name">Player1</span>
      <div class="bubble"></div>
    </div>

    <div id="p2" class="player-card">
      <div class="profile"></div>
      <span class="name">Player2</span>
      <div class="bubble"></div>
    </div>
  </div>

  <div class="emoji-buttons">
    <button type="button" data-emoji="smile">🙂</button>
    <button type="button" data-emoji="angry">😡</button>
    <button type="button" data-emoji="clap">👏</button>
  </div>

  <div id="emoji-ws-status" class="ws-status">EMOJI WS: 준비</div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/chat/emojiChat.css" />

<script>
  window.contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/chat/emojiChat.js"></script>
