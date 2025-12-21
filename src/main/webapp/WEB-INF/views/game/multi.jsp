<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>2 vs 2 Team Omok</title>
<style>
	canvas { border: 1px solid black; background-color: #e3c986; } /* 오목판 색상 추가 */
    #timer { font-size: 20px; font-weight: bold; margin-top: 10px; }
    #status { font-size: 18px; color: blue; font-weight: bold; margin-bottom: 5px; }
</style>
</head>
<body>
	<h1>2:2 팀전 오목 게임</h1>
    <div id="status">대기 중...</div>
	<canvas id="board" width="450" height="450"></canvas>
	<div id="log" style="height: 150px; overflow-y: auto; border: 1px solid #ccc; padding: 5px;"></div>
	<div id="timer"></div>
	
    <button onclick="giveUp()" style="margin-top:10px; padding: 5px 10px;">기권하기</button>

	<script>
	const canvas = document.getElementById("board");
	const ctx = canvas.getContext("2d");
	const size = 30;
    const statusDiv = document.getElementById("status");
	
 	// 전역 변수 초기화
    let myIdx = -1;   // 플레이어 번호 (0~3) 저장
    let myColor = 0;  // 1: 흑, 2: 백
    let isMyTurn = false;
    let gameOver = false;
    let remainsec = 0;
    let timer = null;

    drawBoard();

    const params = new URLSearchParams(window.location.search);
	const playType = params.get("playType");  // "single" | "multi"
	const roomId = params.get("roomId");      // "게임_아이디"
	
	if(!roomId) {
		alert("roomId가 없습니다. URL에 roomId를 포함해 주세요.");
		throw new Error("Missing roomId");
	}
	
	if (playType !== "multi") {
		console.warn("playType이 multi가 아닙니다.:", playType);
	}
	
	const wsProtocol = (location.protocl === "https:") ? "wss" : "ws";
	const contextPath = "<%= request.getContextPath() %>";
	const wsUrl = 
		wsProtocol + "://" +
		location.host +
		contextPath +
		"/game/multi?roomId=" +
		encodeURIComponent(roomId);
	console.log("접속 시도 URL:", wsUrl);
	const ws = new WebSocket(wsUrl);
	
	ws.onopen = () => log("서버에 연결되었습니다. 매칭을 기다립니다...");
	ws.onmessage = (e) => handle(JSON.parse(e.data));
    ws.onerror = (e) => console.error("WebSocket error", e);
    ws.onclose = () => {
        log("연결이 종료되었습니다.");
        statusDiv.innerText = "연결 끊김";
    };

	function handle(data) {
		// 0. 플레이어 입장 대기
		if (data.type === "MULTI_WAIT") {
			statusDiv.innerText = data.msg;
			log(data.msg);
			return;
		}
		
	    // 1. 게임 시작
	    if (data.type === "GAME_MULTI_START") {
	    	drawBoard();
	    	
	    	myIdx = data.slot;
	    	myColor = data.color;
	    	
            let colorName = (myColor === 1 ? "흑돌(선공)" : "백돌(후공)");
            
            // 사용자에게 보여줄 때만 'myIdx + 1'
            let displayIdx = myIdx + 1;
            
            log("게임 시작! 당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.");
            statusDiv.innerText = "당신은 " + colorName + " 팀 소속, " + displayIdx + "번째 순서입니다.";
	    }
	    
	    // 2. 턴 변경 알림
	    if (data.type === "MULTI_TURN") {
	    	if (gameOver) {
	    		return;
	    	}
            
            isMyTurn = (data.turnIdx === myIdx);
            
            startTimer(data.time, data.color);
            
            let turnColorName = (data.color === 1 ? "흑돌" : "백돌");
            let displayTurnIdx = data.turnIdx + 1; // 상대방 번호 보여줄 때 +1

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
	
        // 3. 돌 두기
	    if (data.type === "MULTI_STONE") {
	        drawStone(data.x, data.y, data.color);
	    }
	    
        // 4. 승리/패배 처리
	    if (data.type === "MULTI_WIN") {
	    	gameOver = true;
            clearInterval(timer);
	    	let winColor = data.color;
            let msg = (winColor === 1 ? "흑돌 팀 승리!!" : "백돌 팀 승리!!");
            
            if (myColor === winColor) {
                alert("승리했습니다! 축하합니다.");
            } else {
                alert("패배했습니다.");
            }
            log(msg);
            statusDiv.innerText = msg;
            
            goToRoomView();
	    }
	    
        // 5. 에러/종료 메시지
	    if (data.type === "error" || data.type === "GAME_OVER") {
	    	gameOver = true;
            clearInterval(timer);
	    	log(data.msg);
            alert(data.msg);
            
            if (data.type === "GAME_OVER") {
            	goToRoomView();
            }
	    }
	}
	
	function giveUp() {
		if(confirm('정말 기권하시겠습니까?')) {
            ws.send(JSON.stringify({ type: "MULTI_GIVEUP" }));
        }
	}
	
	// 오목판 클릭 이벤트
	canvas.addEventListener("click", e => {
        if (ws.readyState !== WebSocket.OPEN || gameOver || !isMyTurn) {
            return;
        }

        const rect = canvas.getBoundingClientRect();
        const x = Math.floor((e.clientX - rect.left) / size);
        const y = Math.floor((e.clientY - rect.top) / size);
        
        // 범위 체크
        if(x >= 0 && x < 15 && y >= 0 && y < 15) {
            ws.send(JSON.stringify({x: x, y: y})); 
        }
    });
	
	// 방 이동 함수
	function goToRoomView() {
	    setTimeout(() => {
	        location.href = "<%= request.getContextPath() %>/room?roomId=" + encodeURIComponent(roomId) + "&playType=" + encodeURIComponent(playType);
	    }, 500);
	}
	
	function startTimer(sec, turnColor) {
	    clearInterval(timer);
	    remainsec = sec;
	    updateTimerText(turnColor, remainsec);
        
	    timer = setInterval(() => {
	        remainsec--;
	        updateTimerText(turnColor, remainsec);
	        if (remainsec <= 0) {
	            clearInterval(timer);
	        }
	    }, 1000);
	}
	
	function updateTimerText(color, sec) {
	    const timerDiv = document.getElementById("timer");
	    let colorName = (color === 1 ? "흑돌" : "백돌");
	    timerDiv.innerText = colorName + "턴 | 남은 시간: " + sec + "초";
	    
	    if (sec <= 5) {
	        timerDiv.style.color = "red";
	    } else {
	        timerDiv.style.color = "black";
	    }
	}

	function drawBoard() {
	    ctx.fillStyle = "#e3c986"; // 바닥 색
        ctx.fillRect(0, 0, canvas.width, canvas.height);

	    ctx.beginPath();
	    ctx.lineWidth = 1;
	    ctx.strokeStyle = "#000";
        
        // 오목판은 선 위에 돌을 두므로, 선을 그릴 때 여백(padding)을 고려하거나 
        // 0부터 시작하되 클릭 좌표와 매핑을 잘 해야 함.
	    for (let i = 0; i < 15; i++) {
            // 가로줄
	        ctx.moveTo(size/2, size*i + size/2);
	        ctx.lineTo(size*14 + size/2, size*i + size/2);
            // 세로줄
	        ctx.moveTo(size*i + size/2, size/2);
	        ctx.lineTo(size*i + size/2, size*14 + size/2);
	    }
	    ctx.stroke();
	}
	
	function drawStone(x, y, color) {
	    ctx.beginPath();
        // 좌표에 size/2를 더해 교차점에 돌이 찍히도록 보정
	    ctx.arc(x * size + size/2, y * size + size/2, 12, 0, Math.PI * 2);
	    ctx.fillStyle = (color === 1 ? "black" : "white");
	    ctx.fill();
        // 백돌은 테두리 필요
        if(color === 2) {
            ctx.strokeStyle = "black";
            ctx.stroke();
        }
	}
	
	function log(msg) {
        const logDiv = document.getElementById("log");
	    logDiv.innerHTML += msg + "<br>";
        logDiv.scrollTop = logDiv.scrollHeight; // 자동 스크롤
	}
	</script>
</body>
</html>