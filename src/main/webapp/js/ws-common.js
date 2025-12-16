// roomChat.jsp / gameChat.jsp 공용
let socket = null;

function connectWs(contextPath, roomId) {
  if (socket && socket.readyState === WebSocket.OPEN) return;

  const wsUrl =
    "ws://" + location.host + contextPath + "/ws/room/" + encodeURIComponent(roomId);

  console.log("WebSocket URL =", wsUrl);
  socket = new WebSocket(wsUrl);

  socket.onopen = function () {
    console.log("WebSocket 연결 성공");
  };

  socket.onerror = function (e) {
    console.error("WebSocket 오류", e);
  };

  socket.onclose = function () {
    console.log("WebSocket 연결 종료");
  };

  socket.onmessage = function (e) {
    const msg = e.data;
    console.log("수신:", msg);

    if (msg.startsWith("STATE:")) {
      const phase = msg.substring("STATE:".length).trim();
      if (typeof onStateMessage === "function") onStateMessage(phase);
      return;
    }

    if (typeof onWsMessage === "function") onWsMessage(msg);
  };
}

function sendWs(msg) {
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    alert("WebSocket이 연결되어 있지 않습니다.");
    return;
  }
  socket.send(msg);
}
