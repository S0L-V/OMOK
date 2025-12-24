<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>1 vs 1 omok</title>
<style>
	canvas { border: 1px solid black; background-color: #e3c986; }
</style>
<link rel="stylesheet" href="/resources/css/omok.css">
</head>
<body>
	<div class="game-wrap">
    <h1 class="game-title">1 vs 1 OMOK</h1>

    <div class="status-bar">
      <div id="status" class="status-text">대기 중...</div>
      <div class="chips">
        <div id="timer" class="chip">-</div>
        <div id="pass" class="chip">-</div>
      </div>
    </div>

    <div class="game-grid">

	  <!-- LEFT: 상대(여기에 #p2가 들어감) -->
	  <section class="card turn-ring" id="leftPanel">
	    <div class="card-header">
	      <div class="card-title">상대</div>
	      <span id="opStoneBadge" class="badge">-</span>
	    </div>
	    <div class="card-body">
	      <div id="opProfileSlot"></div>
	    </div>
	  </section>
	
	  <!-- CENTER: 보드 + 로그 -->
	  <section class="card board-card">
	    <div class="card-header">
	      <div class="card-title">오목판</div>
	      <button class="btn btn-danger" onclick="giveUp()">기권하기</button>
	    </div>
	
	    <!-- ✅ board-card .card-body가 flex-center라서, 캔버스만 둬야 css랑 딱 맞음 -->
	    <div class="card-body">
	      <canvas id="board" width="450" height="450"></canvas>
	    </div>
	
	    <div class="card-body log-body">
	      <div class="card-title log-title">로그</div>
	      <div id="log" class="log"></div>
	    </div>
	  </section>
	
	  <!-- RIGHT: 나(여기에 #p1 + 이모지 버튼들이 들어감) -->
	  <section class="card turn-ring" id="rightPanel">
	    <div class="card-header">
	      <div class="card-title">나</div>
	      <span id="myStoneBadge" class="badge">-</span>
	    </div>
	    <div class="card-body">
	      <div id="myProfileSlot"></div>
	      <div id="myEmojiButtonsSlot" style="margin-top:10px;"></div>
	      <div id="myEmojiStatusSlot" style="margin-top:10px;"></div>
	    </div>
	  </section>
	
	</div>

    <!-- ✅ 프로필/이모지 영역은 지금 단계에서 건드리지 않는다 -->
    <%@ include file="/WEB-INF/views/chat/gameEmoji.jsp" %>
  </div>
  <script>
	document.addEventListener("DOMContentLoaded", () => {
	  const p1 = document.getElementById("p1"); // 내 프로필(기존)
	  const p2 = document.getElementById("p2"); // 상대 프로필(기존)
	
	  const btns = document.querySelector(".emoji-buttons"); // 🙂😡👏 버튼 묶음
	  const status = document.getElementById("emoji-ws-status"); // 상태
	
	  const leftSlot  = document.getElementById("opProfileSlot");
	  const rightSlot = document.getElementById("myProfileSlot");
	  const btnSlot   = document.getElementById("myEmojiButtonsSlot");
	  const stSlot    = document.getElementById("myEmojiStatusSlot");
	
	  if (!p1 || !p2 || !leftSlot || !rightSlot) {
	    console.warn("이모지 프로필 이동 실패", { p1, p2, leftSlot, rightSlot });
	    return;
	  }
	
	  // ✅ 프로필 이동
	  leftSlot.appendChild(p1);     // 상대 → 왼쪽
	  rightSlot.appendChild(p2);    // 나 → 오른쪽
	
	  // ✅ 버튼/상태도 같이 이동 (이걸 안 하면 네가 누르는 버튼이 '죽은 쪽'일 수 있음)
	  if (btns && btnSlot) btnSlot.appendChild(btns);
	  if (status && stSlot) stSlot.appendChild(status);
	
	  // ✅ 껍데기 wrapper가 남아 공간 차지하면 숨김(단, 버튼/상태 이동 후!)
	  const wrap = document.querySelector(".emoji-game-wrap");
	  if (wrap) wrap.style.display = "none";
	});
	</script>
  
	<script>
 	 	window.loginUserId = "<%= (String)session.getAttribute("loginUserId") %>";
 	 	window.loginNickname = "<%= (String)session.getAttribute("loginNickname") %>";
 	 </script>
	
	<script>
	const statusDiv = document.getElementById("status");
	const timerDiv = document.getElementById("timer");
	const passDiv = document.getElementById("pass");
	const canvas = document.getElementById("board");
	const ctx = canvas.getContext("2d");
	const size = 30;
	
	let myColor = 0;
	let winner = 0;
	let gameOver = false;
	let remainsec = 0;
	let timer = null;
	
	const myStoneBadge = document.getElementById("myStoneBadge"); 
	const opStoneBadge = document.getElementById("opStoneBadge");
	
	drawBoard();
	
	const params = new URLSearchParams(window.location.search);
	const playType = params.get("playType");  // "single" | "multi"
	const roomId = params.get("roomId");      // "게임_아이디"

	if (!roomId) {
	  alert("roomId가 없습니다. URL에 roomId를 포함하세요.");
	  throw new Error("Missing roomId");
	}

	if (playType !== "single") {
	  console.warn("playType이 single이 아닙니다:", playType);
	}

	const wsProtocol = (location.protocol === "https:") ? "wss" : "ws";
	const contextPath = "<%= request.getContextPath() %>";

	const wsUrl =
		wsProtocol + "://" +
	    location.host +
	    contextPath +
	    "/single?roomId=" +
	    encodeURIComponent(roomId);
	  
	const ws = new WebSocket(wsUrl);
	window.singleWs = ws; // 이모티콘 채팅에서 handle() 받기위해 전역으로

	
	ws.onopen = () => log("서버에 연결되었습니다. 매칭을 기다립니다...");
	ws.onmessage = e => handle(JSON.parse(e.data)); // 객체로 받음
	ws.onclose = () => {
        log("연결이 종료되었습니다.");
        statusDiv.innerText = "연결 끊김";
    };
	
	function handle(data) {
		if (data.type === "EMOJI_CHAT") {
			  /* emojiChat.js가 화면 표시 담당 */
			  if (window.onEmojiChat) window.onEmojiChat(data.payload);
			  return;
			}
		
		if (data.type === "SINGLE_USER") {
		    if (window.onSingleUser) window.onSingleUser(data.payload); 
		    return;
		  }

		if (data.type === "SINGLE_WAIT") {
			statusDiv.innerText = data.msg;
			log(data.msg);
			return;
		}
		
	    if (data.type === "SINGLE_START") {
	        myColor = data.color;
	       	window.myColor = myColor; // 전역으로
	       	
	       	if (myColor === 1) { 
	       		myStoneBadge.className = "badge black"; 
	       		myStoneBadge.innerText = "흑돌"; 
	       		opStoneBadge.className = "badge white"; 
	       		opStoneBadge.innerText = "백돌"; 
	       	} else { 
	       		myStoneBadge.className = "badge white"; 
	       		myStoneBadge.innerText = "백돌";
	       		opStoneBadge.className = "badge black"; 
	       		opStoneBadge.innerText = "흑돌"; 
	       	}
	       	
	       	if (window.setEmojiNames) window.setEmojiNames();
	        log(myColor === 1 ? "당신은 흑돌" : "당신은 백돌");
	        drawBoard();
	    }
	    
	    if (data.type === "SINGLE_TURN") {
	    	if (gameOver) return; 
	    	startTimer(data.time, data.color);
	    	let passnum = (myColor == 1 ? (3 - data.bpass) : (3 - data.wpass));
	    	passDiv.innerText = (myColor == 1?"흑":"백")+"돌님! 패스 횟수 " + passnum + "회 남았습니다!";
    		if (passnum < 2) {
   		        passDiv.style.color = "red";
   		    } else {
   		        passDiv.style.color = "black";
   		    }
    		
    		if (data.color == myColor) { 
    			statusDiv.innerText = "나의 차례입니다!"; 
    			statusDiv.style.color = "red"; 
    		} else { 
    			statusDiv.innerText = "상대방 차례입니다."; 
    			statusDiv.style.color = "blue"; 
    		}
	    }
	
	    if(data.type === "delay") {
	    	clearInterval(timer);
	    	passDiv.innerText = (data.delayColor === 1 ? "흑돌 시간초과로 패스입니다!" : "백돌 시간초과로 패스입니다!");
	    	const timerDiv = document.getElementById("timer"); // 추가
	    	timerDiv.innerText = (data.delayColor === 1 ? "다음차례: 백돌" : "다음차례: 흑돌");
	    }
	    
	    if (data.type === "SINGLE_STONE") {
	        drawStone(data.x, data.y, data.color);
	    }
	    
	    if (data.type === "SINGLE_WIN") {
	    	gameOver = true;
	    	log("게임 종료")
	    	winner = data.color;
	    	passDiv.innerText = (winner === 1 ? "흑돌이 이겼습니다!!" : "백돌이 이겼습니다!!");
	    	clearInterval(timer);
	    	goToRoomView();
	    	
	    }
	    
	    if (data.type === "SINGLE_GIVEUP") {
	    	gameOver = true;
	    	log("게임 종료")
	    	winner = data.losercolor;
	    	if(winner){
	    		passDiv.innerText = (winner === 1 ? "흑돌이 기권했습니다!! 백돌 승!!" : "백돌이 기권했습니다!! 흑돌 승!!");
	    	}
	    	clearInterval(timer);
	    	goToRoomView();
	    }
	}
	
	/* 기권 */ 
	function giveUp() {
		if(confirm('기권하시겠습니까?')) {
			ws.send(JSON.stringify({ type: "SINGLE_GIVEUP" }));
		}
	}
	
	/* 방으로 이동 */
	function goToRoomView() {
	    // (선택) 웹소켓 닫아주면 더 깔끔
	    try { ws.close(); } catch(e) {}

	    fetch(contextPath + "/room/playersToRoom", {
	        method: "POST",
	        headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
	        body: "roomId=" + encodeURIComponent(roomId)
	    })
	    .then(res => res.json())
	    .then(data => {
	        if (!data.ok) throw new Error(data.message || "update failed");

	        setTimeout(() => {
	            location.href = contextPath + "/room?roomId=" + encodeURIComponent(roomId) + "&playType=0";
	        }, 3000);
	    })
	    .catch(err => {
	        console.error(err);
	        // 실패해도 일단 방으로 보내고 싶으면 이렇게:
	        setTimeout(() => {
	            location.href = contextPath + "/room?roomId=" + encodeURIComponent(roomId) + "&playType=0";
	        }, 3000);
	    });
	}
	
	/* 보드 클릭(돌 두기) */
	canvas.addEventListener("click", e => {
		if (gameOver) return;
	    const x = Math.round(e.offsetX / size);
	    const y = Math.round(e.offsetY / size);
	    ws.send(JSON.stringify({x, y})); //문자열로 보내야함
	});
	
	/* 타이머 */
	function startTimer(sec, turnColor) {
	    clearInterval(timer);
	    remainsec = sec;
	
	    updateTimerText(turnColor, remainsec);
	
	    timer = setInterval(() => {
	        remainsec--;
	
	        updateTimerText(turnColor, remainsec);
	
	        if (remainsec < 0) {
	            clearInterval(timer);
	        }
	    }, 1000);
	}
	
	/* 타이머 출력 */
	function updateTimerText(color, sec) {
		
	    let colorName = color === 1 ? "흑돌" : "백돌";
	    timerDiv.innerText = colorName + " 차례 | 남은 시간: " + sec + "초";
	
	    if (sec <= 5) {
	        timerDiv.style.color = "red";
	    } else {
	        timerDiv.style.color = "black";
	    }
	}
	
	/* 그리기 */
	function drawBoard() {
		ctx.fillStyle = "#e3c986"; // 바닥 색
        ctx.fillRect(0, 0, canvas.width, canvas.height);

	    for (let i = 0; i < 15; i++) {
	    	ctx.moveTo(size*i, 0);
	        ctx.lineTo(size*i, 450);
	        ctx.moveTo(0, size*i);
	        ctx.lineTo(450, size*i);
	    }
	    ctx.stroke();
	}
	
	/* 턴 강조 */
	function setTurnHighlight(turnColor){ // 내 색: myColor 
		const isMyTurn = (turnColor === myColor); 
		const p1 = document.getElementById("p1"); // (emojiChat.js 기준) p1=나 
		const p2 = document.getElementById("p2"); // p2=상대 
		if (!p1 || !p2) return; 
		p1.classList.toggle("turn-active", isMyTurn); 
		p2.classList.toggle("turn-active", !isMyTurn); 
	}
	
	function drawStone(x, y, color) {
	    ctx.beginPath();
	    ctx.arc(x * size, y * size, 12, 0, Math.PI * 2);
	    ctx.fillStyle = color === 1 ? "black" : "white";
	    ctx.fill();
	    ctx.stroke();
	}
	
	function log(msg) {
	    document.getElementById("log").innerHTML += msg + "<br>";
	}
	
	</script>
	
<!-- 	 <div id="emojiArea"></div> -->

  <%//@ include file="/WEB-INF/views/chat/gameEmoji.jsp" %>
</body>
</html>
