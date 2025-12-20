(() => {
  const pageEl = document.querySelector("#room-page");
  const roomId = pageEl?.dataset?.roomId || "";
  const roomName = pageEl?.dataset?.roomName || "";

  const wsStatus = document.querySelector("#ws-status");
  const chatInput = document.querySelector("#chat-input");
  const chatLog = document.querySelector("#chat-log");
  const userList = document.querySelector("#user-list");

  const btnLobby = document.querySelector("#btn-lobby");
  const btnLeave = document.querySelector("#btn-leave");
  const btnSend = document.querySelector("#chat-send");

  let ws = null;

  // 클라이언트가 관리하는 현재 참가자 맵 (userId -> nickname)
  const users = new Map();

  function setStatus(text) {
    if (wsStatus) wsStatus.textContent = text;
  }

  function wsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws/room?roomId=" + encodeURIComponent(roomId);
  }

  function safeJsonParse(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      console.warn("[WS] invalid json:", raw);
      return null;
    }
  }

  function send(type, payload = {}) {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(JSON.stringify({ type, payload }));
  }

  function escapeHtml(str) {
    return String(str)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function renderUsers() {
    if (!userList) return;

    if (users.size === 0) {
      userList.innerHTML = `<li class="muted">현재 참가자가 없습니다.</li>`;
      return;
    }

    // 닉네임 기준으로 보기 좋게 정렬 (원하면 userId 기준으로 바꿔도 됨)
    const arr = Array.from(users.entries()).map(([userId, nickname]) => ({
      userId,
      nickname: nickname || userId || "unknown",
    }));
    arr.sort((a, b) => a.nickname.localeCompare(b.nickname, "ko"));

    userList.innerHTML = arr
      .map(
        (u) =>
          `<li data-user-id="${encodeURIComponent(u.userId)}">${escapeHtml(u.nickname)}</li>`
      )
      .join("");
  }

  function addUser(userId, nickname) {
    if (!userId) return;
    users.set(String(userId), nickname ? String(nickname) : "");
    renderUsers();
  }

  function removeUser(userId) {
    if (!userId) return;
    users.delete(String(userId));
    renderUsers();
  }

  function appendSystemLog(text) {
    if (!chatLog) return;
    const div = document.createElement("div");
    div.className = "muted";
    div.textContent = text;
    chatLog.appendChild(div);
    chatLog.scrollTop = chatLog.scrollHeight;
  }

  function appendChat(from, text) {
    if (!chatLog) return;

    const row = document.createElement("div");
    row.className = "chat-row";
    row.innerHTML = `<b>${escapeHtml(from || "unknown")}</b> ${escapeHtml(text || "")}`;

    chatLog.appendChild(row);
    chatLog.scrollTop = chatLog.scrollHeight;
  }

  function handleError(msg) {
    // 서버가 {type:"ERROR", payload:{code,message}} 형태로 보내는 기준
    const code = msg?.payload?.code ?? msg?.code;
    const message = msg?.payload?.message ?? msg?.message ?? "오류가 발생했습니다.";
    console.warn("[WS] ERROR:", code, message);
    alert(message);
    location.href = "/lobby";
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

      // ✅ 서버가 {type,payload} 구조를 기대하므로 payload로 보냄
      send("ROOM_ENTER", { roomId });

      // 참가자 목록은 ROOM_USERS가 없으니,
      // 최소한 "내 정보"는 CONNECTED payload로 받으면 즉시 목록에 넣어보자.
      // (서버에서 CONNECTED가 먼저 오고, 이후 USER_ENTER가 올 수도 있음)
    };

    ws.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;

      switch (msg.type) {
        case "CONNECTED": {
          // payload: {login,userId,nickname,role,roomId}
          const p = msg.payload || {};
          if (p.login && p.userId) {
            addUser(p.userId, p.nickname);
          }
          // (선택) 연결 로그
          // appendSystemLog("WS 연결됨");
          break;
        }

        case "USER_ENTER": {
          const p = msg.payload || {};
          if (p.userId) {
            addUser(p.userId, p.nickname);
            appendSystemLog(`[입장] ${p.nickname || p.userId}`);
          }
          break;
        }

        case "USER_EXIT": {
          const p = msg.payload || {};
          if (p.userId) {
            removeUser(p.userId);
            appendSystemLog(`[퇴장] ${p.nickname || p.userId}`);
          }
          break;
        }

        case "ROOM_CHAT": {
          const p = msg.payload || {};
          appendChat(p.from, p.text);
          break;
        }

        case "ROOM_EXIT": {
          // 서버가 ACK로 보내면 받을 수 있음
          appendSystemLog("방에서 나갔습니다.");
          break;
        }

        case "ERROR": {
          handleError(msg);
          break;
        }

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
    send("ROOM_EXIT", {});
    try {
      ws?.close();
    } catch (_) {}
    location.href = "/lobby";
  });

  btnSend?.addEventListener("click", () => {
    const text = chatInput?.value?.trim();
    if (!text) return;

    // ✅ 서버는 payload.text를 읽으니까 payload로 보냄
    send("ROOM_CHAT", { text });
    chatInput.value = "";
  });

  chatInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") btnSend?.click();
  });

  window.addEventListener("beforeunload", () => {
    try {
      ws?.close();
    } catch (_) {}
  });

  // 초기 렌더
  renderUsers();
  connect();
})();
