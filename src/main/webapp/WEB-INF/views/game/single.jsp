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
</head>
<body>
	<h1>오목 게임</h1>
	<canvas id="board" width="450" height="450"></canvas>
	<div id="log"></div>
	<div id="timer" style="font-size:20px; font-weight:bold;"></div>
	<div id="pass" style="font-size:20px; font-weight:bold;"></div>
	
	<button onclick="giveUp()" style="margin-top:10px; padding: 5px 10px;">기권하기</button>
	<script>
	const passDiv = document.getElementById("pass");
	const canvas = document.getElementById("board");
	const ctx = canvas.getContext("2d");
	const size = 30;
	
	let myColor = 0;
	let winner = 0;
	let gameOver = false;
	let remainsec = 0;
	let timer = null;
	
	drawBoard();
	
	let ws = new WebSocket(
	    "ws://localhost:8081/omok"
	);
	
	
	ws.onopen = () => log("서버 연결");
	ws.onmessage = e => handle(JSON.parse(e.data)); //객체로 받음
	
	function handle(data) {
	    if (data.type === "SINGLE_START") {
	        myColor = data.color;
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
	    }
	
	    if(data.type === "delay") {
	    	passDiv.innerText = (data.delayColor === 1 ? "흑돌 시간초과로 패스입니다! 다음차례: 백돌" : "백돌 시간초과로 패스입니다! 다음차례: 흑돌");
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
	    	
	    }
	    
	    if (data.type === "SINGLE_GIVEUP") {
	    	gameOver = true;
	    	log("게임 종료")
	    	winner = data.losercolor;
	    	if(winner){
	    		passDiv.innerText = (winner === 1 ? "흑돌이 기권했습니다!! 백돌 승!!" : "백돌이 기권했습니다!! 흑돌 승!!");
	    	}
	    	clearInterval(timer);
	    }
	}
	
	/* 기권 */ 
	function giveUp() {
		if(confirm('기권하시겠습니까?')) {
			ws.send(JSON.stringify({ type: "SINGLE_GIVEUP" }));
			//ws.send(JSON.stringify({command: "giveup"}));
			//방 링크: location.href = '/pro17/member/delMember.do?id=' + id;
		}
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
	    const timerDiv = document.getElementById("timer");
	
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
</body>
</html>