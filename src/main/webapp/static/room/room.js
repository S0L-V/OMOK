(() => {
  const pageEl = document.querySelector("#room-page");
  const roomId = pageEl?.dataset?.roomId || "";
  const roomName = pageEl?.dataset?.roomName || "";

  const wsStatus = document.querySelector("#ws-status");
  const chatInput = document.querySelector("#chat-input");

  const btnLobby = document.querySelector("#btn-lobby");
  const btnLeave = document.querySelector("#btn-leave");
  const btnSend = document.querySelector("#chat-send");

  let ws = null;

  function setStatus(text) {
    if (wsStatus) wsStatus.textContent = text;
  }

  function wsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws/room?roomId=" + roomId;
  }

  function safeJsonParse(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      console.warn("[WS] invalid json:", raw);
      return null;
    }
  }

  function send(obj) {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(JSON.stringify(obj));
  }


  function connect() {
    if (!roomId) {
      setStatus("WS: roomId 없음");
      return;
    }

    const url = wsUrl();
    console.log("[WS] connecting:", url, "roomId=", roomId, "roomName=", roomName);

    setStatus("WS: 연결 중...");
    ws = new WebSocket(url);

    ws.onopen = () => {
      setStatus("WS: 연결됨");
      send({ type: "ROOM_ENTER", roomId });
    };

    ws.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;

      switch (msg.type) {
        case "CONNECTED":
          break;
          
        case "ERROR":
          alert(msg.message || "오류가 발생했습니다.");
          location.href = "/lobby";
          break;

        default:
          console.log("[WS] message:", msg);
      }
    };

    ws.onclose = () => setStatus("WS: 종료됨");
    ws.onerror = () => setStatus("WS: 에러");
  }

  btnLobby?.addEventListener("click", () => {
    location.href = "/lobby";
  });

  btnLeave?.addEventListener("click", () => {
    send({ type: "ROOM_EXIT" });
    try { ws?.close(); } catch (_) {}
    location.href = "/lobby";
  });

  btnSend?.addEventListener("click", () => {
    const text = chatInput?.value?.trim();
    if (!text) return;

    send({ type: "ROOM_CHAT", roomId, text });
    chatInput.value = "";
  });

  chatInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") btnSend?.click();
  });

  window.addEventListener("beforeunload", () => {
    try { ws?.close(); } catch (_) {}
  });

  connect();
})();
