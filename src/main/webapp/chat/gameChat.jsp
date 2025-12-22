<%@ page contentType="text/html; charset=UTF-8" %>

<!-- 게임 중 이모지 전용 UI -->
<div class="emoji-game-wrap">
  <h3 class="emoji-title">게임 중 (이모지 전용)</h3>

  <div class="player-cards">
    <!-- Player 1 -->
    <div id="p1" class="player-card">
      <div class="profile"></div>
      <span class="name">Player1</span>
      <div class="bubble"></div>
    </div>

    <!-- Player 2 -->
    <div id="p2" class="player-card">
      <div class="profile"></div>
      <span class="name">Player2</span>
      <div class="bubble"></div>
    </div>
  </div>

  <!-- 이모지 버튼 -->
  <div class="emoji-buttons">
    <button onclick="sendEmoji('smile')">🙂</button>
    <button onclick="sendEmoji('angry')">😡</button>
    <button onclick="sendEmoji('clap')">👏</button>
  </div>

  <div id="ws-status" class="ws-status">WS: 준비</div>
</div>

<!-- 이모지 전용 JS -->
<script src="${pageContext.request.contextPath}/static/game/game.js"></script>

<style>
.emoji-game-wrap { margin-top:20px; }
.emoji-title { font-weight:bold; margin-bottom:12px; }

.player-cards {
  display:flex;
  gap:20px;
  margin-bottom:14px;
}

.player-card {
  position:relative;
  display:flex;
  align-items:center;
  gap:10px;
  padding:12px 16px;
  border:1px solid #ddd;
  border-radius:14px;
  min-width:200px;
}

.profile {
  width:40px;
  height:40px;
  border-radius:50%;
  background:#eee;
}

.name { font-weight:600; }

.bubble {
  position:absolute;
  right:-14px;
  top:-14px;
  padding:6px 10px;
  background:#fff;
  border:1px solid #ddd;
  border-radius:12px;
  font-size:20px;
  display:none;
  box-shadow:0 2px 6px rgba(0,0,0,0.15);
}

.emoji-buttons button {
  font-size:22px;
  padding:6px 10px;
  margin-right:8px;
  border-radius:8px;
  border:1px solid #ddd;
  background:#fff;
  cursor:pointer;
}

.emoji-buttons button:hover {
  background:#f5f5f5;
}

.ws-status {
  margin-top:8px;
  font-size:12px;
  color:#666;
}
</style>