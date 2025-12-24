<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>2 vs 2 Team Omok</title>

<!-- 정적 리소스 경로: /static/chat/... -->
<link rel="stylesheet" href="<%= request.getContextPath() %>/static/chat/emojiChat.css" />
<link rel="stylesheet" href="/resources/css/omok.css">
</head>

<body>
<script>
  window.loginUserId = "<%= (String)session.getAttribute("loginUserId") %>";
  window.loginNickname = "<%= (String)session.getAttribute("loginNickname") %>";
  window.contextPath = "<%= request.getContextPath() %>";
</script>

<div class="game-wrap">
  <h1 class="game-title">2 vs 2 TEAM OMOK</h1>

  <div class="status-bar">
    <div id="status" class="status-text">대기 중...</div>
    <div class="chips">
      <div id="timer" class="chip">-</div>
    </div>
  </div>

  <div class="game-grid">

    <!-- LEFT: 상대팀 -->
    <section class="card turn-ring" id="leftPanel">
      <div class="card-header">
        <div class="card-title">상대팀</div>
        <span id="opTeamBadge" class="badge">-</span>
      </div>
      <div class="card-body">
        <div id="opTopSlot"></div>
        <div style="height:12px;"></div>
        <div id="opBottomSlot"></div>
      </div>
    </section>

    <!-- CENTER: 보드 + 로그 -->
    <section class="card board-card">
      <div class="card-header">
        <div class="card-title">오목판</div>
        <button class="btn btn-danger" onclick="giveUp()">기권하기</button>
      </div>

      <div class="card-body">
        <canvas id="board" width="450" height="450"></canvas>
      </div>

      <div class="card-body log-body">
        <div class="card-title log-title">로그</div>
        <div id="log" class="log"></div>
      </div>
    </section>

    <!-- RIGHT: 우리팀 (위=팀원 / 아래=나) -->
    <section class="card turn-ring" id="rightPanel">
      <div class="card-header">
        <div class="card-title">우리팀</div>
        <span id="myTeamBadge" class="badge">-</span>
      </div>
      <div class="card-body">
        <div id="allyTopSlot"></div>
        <div style="height:12px;"></div>
        <div id="mySlot"></div>

        <!-- ✅ 이모지 버튼은 "나" 카드 옆(오른쪽 패널) 아래에 배치 -->
        <div style="height:12px;"></div>
        <div id="myEmojiButtonsSlot"></div>
        <div id="myEmojiStatusSlot" style="margin-top:10px;"></div>
      </div>
    </section>

  </div>

  <!-- ✅ 기존 p1~p4는 삭제하지 말고 유지(단, 화면엔 안 보이게) -->
  <div style="display:none">
    <div id="p1" class="player-card" data-slot="0">
      <div class="profile"></div><div class="name">P1</div><div class="bubble"></div>
    </div>
    <div id="p2" class="player-card" data-slot="3">
      <div class="profile"></div><div class="name">P2</div><div class="bubble"></div>
    </div>
    <div id="p3" class="player-card" data-slot="1">
      <div class="profile"></div><div class="name">P3</div><div class="bubble"></div>
    </div>
    <div id="p4" class="player-card" data-slot="2">
      <div class="profile"></div><div class="name">P4</div><div class="bubble"></div>
    </div>

    <!-- 기존 이모지 UI도 여기서 유지(스크립트가 찾게) -->
    <div class="emoji-game-wrap">
      <div class="emoji-buttons">
        <button type="button" data-emoji="smile">🙂</button>
        <button type="button" data-emoji="angry">😡</button>
        <button type="button" data-emoji="clap">👏</button>
      </div>
      <div id="emoji-ws-status" class="ws-status">EMOJI: 준비</div>
    </div>
  </div>
  <!-- ✅ 게임 시작 카운트다운 오버레이 -->
	<div id="startOverlay" class="start-overlay hidden">
	  <div class="start-box">
	    <div id="startColorText" class="start-color">-</div>
	    <div id="startCount" class="start-count">3</div>
	    <div class="start-sub">곧 게임이 시작됩니다</div>
	  </div>
	</div>
</div>

<script>
document.addEventListener("DOMContentLoaded", () => {
  // 1) 기존 카드들
  const p1 = document.getElementById("p1"); // slot 0
  const p2 = document.getElementById("p2"); // slot 3
  const p3 = document.getElementById("p3"); // slot 1
  const p4 = document.getElementById("p4"); // slot 2

  // 2) 멀티 슬롯들(네가 만든 div id들)
  const opTopSlot    = document.getElementById("opTopSlot");
  const opBottomSlot = document.getElementById("opBottomSlot");
  const allyTopSlot  = document.getElementById("allyTopSlot");
  const mySlot       = document.getElementById("mySlot");

  // 3) 이모지 UI
  const btns   = document.querySelector(".emoji-buttons");
  const status = document.getElementById("emoji-ws-status");
  const btnSlot = document.getElementById("myEmojiButtonsSlot");
  const stSlot  = document.getElementById("myEmojiStatusSlot");

  if (!p1 || !p2 || !p3 || !p4 || !opTopSlot || !opBottomSlot || !allyTopSlot || !mySlot) {
    console.warn("멀티 프로필 이동 실패", { p1, p2, p3, p4, opTopSlot, opBottomSlot, allyTopSlot, mySlot });
    return;
  }

  // ✅ 여기서는 '임시 배치'만 해둠(일단 화면에 뜨게)
  //   실제로 '누가 나냐'는 GAME_MULTI_START에서 myIdx 받고 다시 재배치함
  opTopSlot.appendChild(p1);
  opBottomSlot.appendChild(p4);
  allyTopSlot.appendChild(p3);
  mySlot.appendChild(p2);

  // ✅ 이모지 버튼/상태도 슬롯으로 이동 (버튼 죽는 문제 방지)
  if (btns && btnSlot) btnSlot.appendChild(btns);
  if (status && stSlot) stSlot.appendChild(status);

  // ✅ 원래 wrapper 숨김(선택) - 단, btn/status 이동 후!
  const wrap = document.querySelector(".emoji-game-wrap");
  if (wrap) wrap.style.display = "none";
});
</script>

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
	 
	 let gameLocked = true; // ✅ 카운트다운 끝나기 전까지 잠금
	 let countdownTimer = null;
	
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
	
	function idxToCard(idx){
	  // 현재 멀티 원본 기준 매핑: 0->p1, 1->p3, 2->p4, 3->p2
	  const map = {0:"p1", 1:"p3", 2:"p4", 3:"p2"};
	  return document.getElementById(map[idx]);
	}
  
  	function isMyTeam(idx){
	  // 현재 로직 그대로: 짝/홀 팀
	  return (idx % 2) === (myIdx % 2);
	}
	
	function setCuteAvatarsAndTeamBadge(){
	  const myTeamBadge = document.getElementById("myTeamBadge");
	  const opTeamBadge = document.getElementById("opTeamBadge");
	
	  // 팀 배지(헤더) - 내 팀이 흑/백인지
	  if (myColor === 1) {
	    myTeamBadge.className = "badge black";
	    myTeamBadge.innerText = "흑팀";
	    opTeamBadge.className = "badge white";
	    opTeamBadge.innerText = "백팀";
	  } else {
	    myTeamBadge.className = "badge white";
	    myTeamBadge.innerText = "백팀";
	    opTeamBadge.className = "badge black";
	    opTeamBadge.innerText = "흑팀";
	  }
	
	  // 귀여운 이모지 세트 (원하면 바꿔도 됨)
	  const blackSet = ["🐻‍❄️", "🐼"];   // 흑팀 느낌
	  const whiteSet = ["🐰", "🐱"];     // 백팀 느낌
	
	  // 내 팀이 흑인지 백인지에 따라 세트 결정
	  const mySet = (myColor === 1) ? blackSet : whiteSet;
	  const opSet = (myColor === 1) ? whiteSet : blackSet;
	
	  // 팀원 2명 / 상대 2명 인덱스 계산
	  const myTeamIdxs = [0,1,2,3].filter(i => (i % 2) === (myIdx % 2));
	  const opTeamIdxs = [0,1,2,3].filter(i => (i % 2) !== (myIdx % 2));
	
	  // 내 팀 카드 2개에 이모지
	  myTeamIdxs.forEach((idx, k) => {
	    const card = idxToCard(idx);
	    if (!card) return;
	    card.classList.remove("team-white","team-black");
	    card.classList.add(myColor === 1 ? "team-black" : "team-white");
	
	    const profile = card.querySelector(".profile");
	    if (profile) profile.textContent = mySet[k % mySet.length];
	  });
	
	  // 상대 팀 카드 2개에 이모지
	  opTeamIdxs.forEach((idx, k) => {
	    const card = idxToCard(idx);
	    if (!card) return;
	    card.classList.remove("team-white","team-black");
	    card.classList.add(myColor === 1 ? "team-white" : "team-black");
	
	    const profile = card.querySelector(".profile");
	    if (profile) profile.textContent = opSet[k % opSet.length];
	  });
	}
	
  function moveCards(myIdx){
	const enemyTop = document.getElementById("opTopSlot");       
	const enemyBot = document.getElementById("opBottomSlot"); 
	const allyTop  = document.getElementById("allyTopSlot");
	const mySlotEl = document.getElementById("mySlot");
	if(!enemyTop || !enemyBot || !allyTop || !mySlotEl) return;
	
	const myCard = idxToCard(myIdx);
	if (myCard) mySlotEl.appendChild(myCard);
	
	// 팀 판단(일단 짝/홀 팀)
	const allyIdx = [0,1,2,3].find(i => i !== myIdx && (i % 2) === (myIdx % 2));
	const enemyIdxs = [0,1,2,3].filter(i => (i % 2) !== (myIdx % 2));
	
	const allyCard = idxToCard(allyIdx);
	if (allyCard) allyTop.appendChild(allyCard);
	
	const e1 = idxToCard(enemyIdxs[0]);
	const e2 = idxToCard(enemyIdxs[1]);
	if (e1) enemyTop.appendChild(e1);
	if (e2) enemyBot.appendChild(e2);
  }
  
  function setTurn(turnIdx){
	["p1","p2","p3","p4"].forEach(id => document.getElementById(id)?.classList.remove("turn-active"));
	const map = {0:"p1", 1:"p3", 2:"p4", 3:"p2"};
	document.getElementById(map[turnIdx])?.classList.add("turn-active");
  }

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
      
      moveCards(myIdx);
      setCuteAvatarsAndTeamBadge();

      

      const colorName = (myColor === 1 ? "흑돌(선공)" : "백돌(후공)");
      const displayIdx = myIdx + 1;

      log("게임 시작! 당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.");
      statusDiv.innerText = "당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.";
      
      runStartCountdown(myColor);
    }

    if (data.type === "MULTI_TURN") {
      if (gameOver) return;
      
      setTurn(data.turnIdx);

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

   	  // 1) data-slot으로 먼저 찾기
      let card = document.querySelector(`.player-card[data-slot='${slot}']`);

      // 2) 혹시 못 찾으면 id 매핑으로도 찾기
      if (!card) {
        const idMap = {0:"p1", 1:"p3", 2:"p4", 3:"p2"};
        card = document.getElementById(idMap[slot]);
      }

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
	if (ws.readyState !== WebSocket.OPEN || gameOver || gameLocked || !isMyTurn) return;

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

  function playPop(el){
	  if (!el) return;
	  el.classList.remove("pop");
	  // reflow 강제(애니메이션 재시작용)
	  void el.offsetWidth;
	  el.classList.add("pop");
  }

  function runStartCountdown(color){
	  const overlay = document.getElementById("startOverlay");
	  const colorText = document.getElementById("startColorText");
	  const countEl = document.getElementById("startCount");
	
	  if (!overlay || !colorText || !countEl) {
	    // 오버레이가 없으면 그냥 바로 시작
	    gameLocked = false;
	    return;
	  }
	
	  // 색 안내 텍스트
	  if (color === 1) {
	    colorText.textContent = "당신은 흑팀입니다 (선공)";
	    colorText.classList.remove("white");
	    colorText.classList.add("black");
	  } else {
	    colorText.textContent = "당신은 백팀입니다 (후공)";
	    colorText.classList.remove("black");
	    colorText.classList.add("white");
	  }
	
	  // 보여주기 + 잠금
	  overlay.classList.remove("hidden");
	  gameLocked = true;
	
	  let n = 3;
	  countEl.textContent = n;
	  playPop(countEl);
	
	  clearInterval(countdownTimer);
	  countdownTimer = setInterval(() => {
	    n--;
	    if (n <= 0) {
	      clearInterval(countdownTimer);
	      overlay.classList.add("hidden");
	      gameLocked = false;     // ✅ 여기서부터 게임 시작(턴/클릭 허용)
	      return;
	    }
	    countEl.textContent = n;
	    playPop(countEl);
	  }, 1000);
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
 
 <script src="<%= request.getContextPath() %>/static/chat/emojiChatMulti.js"></script>
 
</body>
</html>
