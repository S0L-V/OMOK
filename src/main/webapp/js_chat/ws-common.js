let socket = null;

function connectWs(contextPath, roomId) {
  if (socket && socket.readyState === WebSocket.OPEN) return;

  const protocol = location.protocol === "https:" ? "wss://" : "ws://";
  socket = new WebSocket(protocol + location.host + contextPath + "/ws/room/" + roomId);

  socket.onmessage = (e) => {
    const msg = e.data;

    if (msg.startsWith("STATE:")) {
      onStateMessage(msg.substring(6));
      return;
    }
    onWsMessage(msg);
  };
}

function sendWs(msg) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(msg);
  }
}
