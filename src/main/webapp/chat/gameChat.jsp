<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- 게임 화면 안에 "채팅(이모티콘)" UI만 추가하는 용도 -->
<div class="game-chat">
  <div class="game-chat__header">
    <span>😀 이모티콘</span>
    <span id="game-ws-status" class="muted">WS: 준비</span>
  </div>

  <div id="game-chat-log" class="game-chat__log"></div>

  <div class="game-chat__emoji">
    <!-- 필요하면 더 추가해도 됨 (value가 서버로 전송됨) -->
    <button type="button" class="emoji-btn" data-emoji="😀">😀</button>
    <button type="button" class="emoji-btn" data-emoji="😂">😂</button>
    <button type="button" class="emoji-btn" data-emoji="😡">😡</button>
    <button type="button" class="emoji-btn" data-emoji="👍">👍</button>
    <button type="button" class="emoji-btn" data-emoji="👎">👎</button>
    <button type="button" class="emoji-btn" data-emoji="❤️">❤️</button>
  </div>
</div>

<!-- game.js는 "이모티콘 채팅 전용" 스크립트로 쓸 예정 -->
<script src="${pageContext.request.contextPath}/static/game/game.js"></script>

<style>
  /* 최소 스타일(원하면 css 파일로 옮겨도 됨) */
  .game-chat { border:1px solid #e5e7eb; border-radius:12px; padding:12px; background:#fff; }
  .game-chat__header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
  .game-chat__log { height:140px; overflow:auto; border:1px solid #eee; border-radius:10px; padding:10px; background:#fafafa; }
  .game-chat__emoji { display:flex; flex-wrap:wrap; gap:8px; margin-top:10px; }
  .emoji-btn { padding:8px 10px; border:1px solid #ddd; border-radius:10px; background:#fff; cursor:pointer; }
  .emoji-btn:hover { background:#f5f5f5; }
  .muted { color:#6b7280; font-size:12px; }
  .chat-row { margin-bottom:6px; }
</style>