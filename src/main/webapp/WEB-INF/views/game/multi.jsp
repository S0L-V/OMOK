<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>2 vs 2 Team Omok</title>

<style>
  canvas { border: 1px solid black; background-color: #e3c986; } /* 오목판 색상 */
  #timer { font-size: 20px; font-weight: bold; margin-top: 10px; }
  #status { font-size: 18px; color: blue; font-weight: bold; margin-bottom: 5px; }

  .page-wrap {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 16px;
  }

  .game-layout {
    display: flex;
    justify-content: center; /* 보드 중앙 */
    align-items: flex-start;
    gap: 24px;
    margin-top: 10px;
  }

  .side-col {
    width: 240px;
    min-height: 520px;
    display: flex;
    flex-direction: column;
  }

  .side-col .slot-top { margin-bottom: 14px; }
  .side-col .slot-bottom { margin-top: auto; }

  .board-col {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .player-card {
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border: 1px solid #ddd;
    border-radius: 14px;
    min-width: 200px;
    background: #fff;
  }
  .player-card .profile { width: 40px; height: 40px; border-radius: 50%; background: #eee; }
  .player-card .name { font-weight: 600; }
  .player-card .bubble {
    position: absolute;
    right: -14px;
    top: -14px;
    padding: 6px 10px;
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 12px;
    font-size: 20px;
    display: none;
    box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  }
</style>

<!-- 정적 리소스 경로: /static/chat/... -->
<link rel="stylesheet" href="<%= request.getContextPath() %>/static/chat/emojiChat.css" />
</head>

<body>
<div class="page-wrap">
  <h1>2:2 팀전 오목 게임</h1>
  <div id="status">대기 중...</div>

  <div class="game-layout">
    <div class="side-col">
      <div class="slot-top">
        <div id="p1" class="player-card" data-slot="0">
          <div class="profile"></div>
          <div class="name">P1</div>
          <div class="bubble"></div>
        </div>
      </div>
      <div class="slot-bottom">
        <div id="p4" class="player-card" data-slot="2">
          <div class="profile"></div>
          <div class="name">P4</div>
          <div class="bubble"></div>
        </div>
      </div>
    </div>

    <div class="board-col">
      <canvas id="board" width="450" height="450"></canvas>

      <div id="log" style="width: 450px; height: 150px; overflow-y: auto; border: 1px solid #ccc; padding: 5px; margin-top: 10px;"></div>
      <div id="timer" style="width: 450px;"></div>

      <div class="emoji-game-wrap" style="width: 450px; margin-top: 10px;">
        <div class="emoji-buttons">
          <button type="button" data-emoji="smile">🙂</button>
          <button type="button" data-emoji="angry">😡</button>
          <button type="button" data-emoji="clap">👏</button>
        </div>
        <div id="emoji-ws-status" class="ws-status">EMOJI: 준비</div>
      </div>

      <button onclick="giveUp()" style="margin-top:10px; padding: 5px 10px;">기권하기</button>
    </div>

    <div class="side-col">
      <div class="slot-top">
        <div id="p3" class="player-card" data-slot="1">
          <div class="profile"></div>
          <div class="name">P3</div>
          <div class="bubble"></div>
        </div>
      </div>
      <div class="slot-bottom">
        <div id="p2" class="player-card" data-slot="3">
          <div class="profile"></div>
          <div class="name">P2</div>
          <div class="bubble"></div>
        </div>
      </div>
    </div>
  </div>

<script>
  const canvas = document.getElementById("board");
  const ctx = canvas.getContext("2d");
  const size = 30;
  const statusDiv = document.getElementById("status");

  let myIdx = -1;
  let myColor = 0;
  let isMyTurn = false;
  let gameOver = false;
  let remainsec = 0;
  let timer = null;

  drawBoard();

  const params = new URLSearchParams(window.location.search);
  const playType = params.get("playType");
  const roomId = params.get("roomId");

  if (!roomId) {
    alert("roomId가 없습니다. URL에 roomId를 포함해 주세요.");
    throw new Error("Missing roomId");
  }

  const wsProtocol = (location.protocol === "https:") ? "wss" : "ws";
  const contextPath = "<%= request.getContextPath() %>";
  const wsUrl = wsProtocol + "://" + location.host + contextPath + "/game/multi/ws?roomId=" + encodeURIComponent(roomId);

  const ws = new WebSocket(wsUrl);

  /* emojiChatMulti.js에서 사용 */
  window.singleWs = ws;
  window.contextPath = contextPath;
  window.roomId = roomId;

  ws.onopen = () => log("서버에 연결되었습니다. 매칭을 기다립니다...");
  ws.onmessage = (e) => handle(JSON.parse(e.data));
  ws.onerror = (e) => console.error("WebSocket error", e);
  ws.onclose = () => {
    log("연결이 종료되었습니다.");
    statusDiv.innerText = "연결 끊김";
  };

  function handle(data) {
    if (data.type === "MULTI_WAIT") {
      statusDiv.innerText = data.msg;
      log(data.msg);
      return;
    }

    if (data.type === "GAME_MULTI_START") {
      drawBoard();

      myIdx = data.slot;
      myColor = data.color;

      window.mySlot = myIdx; /* 내 슬롯 */

      const colorName = (myColor === 1 ? "흑돌(선공)" : "백돌(후공)");
      const displayIdx = myIdx + 1;

      log("게임 시작! 당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.");
      statusDiv.innerText = "당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.";
    }

    if (data.type === "MULTI_TURN") {
      if (gameOver) return;

      isMyTurn = (data.turnIdx === myIdx);
      startTimer(data.time, data.color);

      const displayTurnIdx = data.turnIdx + 1;

      if (isMyTurn) {
        statusDiv.innerText = "나의 차례입니다!";
        statusDiv.style.color = "red";
      } else if (data.color === myColor) {
        statusDiv.innerText = "같은 팀 " + displayTurnIdx + "번의 차례입니다.";
        statusDiv.style.color = "blue";
      } else {
        statusDiv.innerText = "상대방(" + displayTurnIdx + "번) 차례입니다.";
        statusDiv.style.color = "black";
      }
    }

    if (data.type === "MULTI_STONE") {
      drawStone(data.x, data.y, data.color);
    }

    if (data.type === "MULTI_WIN") {
      gameOver = true;
      clearInterval(timer);

      const msg = (data.color === 1 ? "흑돌 팀 승리!!" : "백돌 팀 승리!!");
      alert(myColor === data.color ? "승리했습니다! 축하합니다." : "패배했습니다.");
      log(msg);
      statusDiv.innerText = msg;

      goToRoomView();
    }

    if (data.type === "error" || data.type === "GAME_OVER") {
      gameOver = true;
      clearInterval(timer);
      log(data.msg);
      alert(data.msg);

      if (data.type === "GAME_OVER") goToRoomView();
    }

    /* 이모지 수신(JSON) */
    if (data.type === "EMOJI_CHAT") {
      if (typeof window.onEmojiChat === "function") {
        window.onEmojiChat(data.payload || {});
      }
      return;
    }
    
    /* 닉네임/슬롯 수신 */
    if (data.type === "MULTI_USER") {
      const p = data.payload || {};
      const slot = p.slot;
      const nick = p.nickname;

      const card = document.querySelector(`.player-card[data-slot='${slot}']`);
      if (card) {
        const nameEl = card.querySelector(".name");
        if (nameEl && nick) nameEl.textContent = nick;
      }
      return;
    }
  }

  function giveUp() {
    if (confirm("정말 기권하시겠습니까?")) {
      ws.send(JSON.stringify({ type: "MULTI_GIVEUP" }));
    }
  }

  canvas.addEventListener("click", (e) => {
    if (ws.readyState !== WebSocket.OPEN || gameOver || !isMyTurn) return;

    const rect = canvas.getBoundingClientRect();
    const x = Math.floor((e.clientX - rect.left) / size);
    const y = Math.floor((e.clientY - rect.top) / size);

    if (x >= 0 && x < 15 && y >= 0 && y < 15) {
      ws.send(JSON.stringify({ x: x, y: y }));
    }
  });

  function goToRoomView() {
    try { ws.close(); } catch (e) {}

    fetch(contextPath + "/room/playersToRoom", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
      body: "roomId=" + encodeURIComponent(roomId)
    })
      .then(res => res.json())
      .then(data => {
        setTimeout(() => {
          location.href = contextPath + "/room?roomId=" + encodeURIComponent(roomId) + "&playType=1";
        }, 3000);
      })
      .catch(() => {
        setTimeout(() => {
          location.href = contextPath + "/room?roomId=" + encodeURIComponent(roomId) + "&playType=1";
        }, 3000);
      });
  }

  function startTimer(sec, turnColor) {
    clearInterval(timer);
    remainsec = sec;
    updateTimerText(turnColor, remainsec);

    timer = setInterval(() => {
      remainsec--;
      updateTimerText(turnColor, remainsec);
      if (remainsec <= 0) clearInterval(timer);
    }, 1000);
  }

  function updateTimerText(color, sec) {
    const timerDiv = document.getElementById("timer");
    const colorName = (color === 1 ? "흑돌" : "백돌");
    timerDiv.innerText = colorName + "턴 | 남은 시간: " + sec + "초";
    timerDiv.style.color = (sec <= 5 ? "red" : "black");
  }

  function drawBoard() {
    ctx.fillStyle = "#e3c986"; // 바닥 색
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.beginPath();
    ctx.lineWidth = 1;
    ctx.strokeStyle = "#000";

    for (let i = 0; i < 15; i++) {
      ctx.moveTo(size / 2, size * i + size / 2);
      ctx.lineTo(size * 14 + size / 2, size * i + size / 2);
      ctx.moveTo(size * i + size / 2, size / 2);
      ctx.lineTo(size * i + size / 2, size * 14 + size / 2);
    }
    ctx.stroke();
  }

  function drawStone(x, y, color) {
    ctx.beginPath();
    ctx.arc(x * size + size / 2, y * size + size / 2, 12, 0, Math.PI * 2);
    ctx.fillStyle = (color === 1 ? "black" : "white");
    ctx.fill();
    if (color === 2) {
      ctx.strokeStyle = "black";
      ctx.stroke();
    }
  }

  function log(msg) {
    const logDiv = document.getElementById("log");
    logDiv.innerHTML += msg + "<br>";
    logDiv.scrollTop = logDiv.scrollHeight;
  }
</script>

<!-- 단체전 전용 이모지 스크립트 -->
<script src="<%= request.getContextPath() %>/static/chat/emojiChatMulti.js"></script>
</div>
</body>
</html>
