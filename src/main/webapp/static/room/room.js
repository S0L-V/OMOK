(() => {
  const pageEl = document.querySelector("#room-page");
  const roomId = pageEl?.dataset?.roomId || "";
  const roomName = pageEl?.dataset?.roomName || "";
  const userId = pageEl?.dataset?.userId || "";
  
  

  const wsStatus = document.querySelector("#ws-status");
  const chatInput = document.querySelector("#chat-input"); // input
  const chatLog = document.querySelector("#chat-log");
  const userList = document.querySelector("#user-list");

  const btnLobby = document.querySelector("#btn-lobby"); // 없으면 null이어도 OK
  const btnLeave = document.querySelector("#btn-leave");
  const btnSend = document.querySelector("#chat-send");

  let ws = null;

  // client state: userId -> nickname
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

    const arr = Array.from(users.entries()).map(([userId, nickname]) => ({
      userId,
      nickname,
    }));
    arr.sort((a, b) => (a.nickname || "").localeCompare(b.nickname || "", "ko"));

    userList.innerHTML = arr
      .map(
        (u) =>
          `<li data-user-id="${encodeURIComponent(u.userId)}">${escapeHtml(u.nickname || "")}</li>`
      )
      .join("");
  }

  function setUsers(list) {
    users.clear();
    (Array.isArray(list) ? list : []).forEach((u) => {
      const userId = u?.userId;
      const nickname = u?.nickname;
      if (userId) users.set(String(userId), nickname ? String(nickname) : "");
    });
    renderUsers();
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
    const message = msg?.payload?.message ?? msg?.message ?? "오류가 발생했습니다.";
    console.warn("[WS] ERROR:", msg);
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
      send("ROOM_ENTER", { roomId });
    };

    ws.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;

      switch (msg.type) {
        case "CONNECTED": {
          break;
        }

        case "ROOM_PLAYER_LIST": {
          const list = msg.payload?.roomPlayerList ?? [];
          setUsers(list);
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
          appendSystemLog("방에서 나갔습니다.");
          break;
        }
        
        case "HOST_CHANGE": {
          appendSystemLog("방장이 변경되었습니다.");
          const p = msg.payload;
		  const page = document.querySelector("#room-page");
		  const roomId = page.dataset.roomId;
		  const playType = page.dataset.playType;
		  if (p.hostUserId === userId) {
		    const form = document.createElement("form");
		      form.id = "start-form";
		      form.method = "post";
		      form.action = `/game/start?roomId=${encodeURIComponent(roomId)}&playType=${encodeURIComponent(playType)}`;
		
		      const btn = document.createElement("button");
		      btn.type = "submit";
		      btn.id = "btn-start";
		      btn.className = "btn-start";
		      btn.textContent = "🎯 시작하기";
		
		      form.appendChild(btn);
		      document.querySelector(".side-nav")?.appendChild(form);
		  }
		  console.log(p);
		  break;
        }

        case "ERROR": {
          handleError(msg);
          break;
        }
        
        case "GAME_START": {
		  const { roomId, playType } = msg.payload || {};
		  
		  console.log("[WS GAME START]", roomId, playType);
		  if (!roomId || !playType) return;
		  
		  ws.close(1000, "NAVIGATE_TO_GAME");
		
		  if (String(playType) === "0") {
		    location.href = `/game/single?roomId=${encodeURIComponent(roomId)}`;
		  } else if (String(playType) === "1") {
		    location.href = `/game/multi?roomId=${encodeURIComponent(roomId)}`;
		  } else {	
		    console.warn("Unknown playType:", playType);
		  }
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
  
  const startForm = document.querySelector("#start-form");
  startForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(startForm);
    const params = new URLSearchParams(formData);

    try {
      const response = await fetch(startForm.action, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body: params,
      });

      if (!response.ok) {
        const text = await response.text();
        alert(`게임 시작 실패: ${text}`);
      }
      // 성공 시: 서버가 WS로 GAME_START를 보내면
      // room.js의 GAME_START 핸들러가 페이지 이동 처리함 :contentReference[oaicite:2]{index=2}
    } catch (err) {
      console.error("게임 시작 요청 실패:", err);
      alert("게임 시작 요청 중 오류가 발생했습니다.");
    }
  });

  /* IME 조합 상태 추적 (한글 뒷글자 중복 방지) */
  let isComposing = false;
  chatInput?.addEventListener("compositionstart", () => {
    isComposing = true;
  });
  chatInput?.addEventListener("compositionend", () => {
    isComposing = false;
  });

  /* 전송 버튼 클릭 */
  btnSend?.addEventListener("click", () => {
    if (isComposing) return;

    const text = chatInput?.value?.trim();
    if (!text) return;

    send("ROOM_CHAT", { text });
    chatInput.value = "";
    chatInput.focus();
  });

  /* Enter 전송 */
  chatInput?.addEventListener("keydown", (e) => {
    if (e.key !== "Enter") return;
    if (isComposing || e.isComposing) return;

    e.preventDefault();
    btnSend?.click();
  });

  window.addEventListener("beforeunload", () => {
    try {
      ws?.close();
    } catch (_) {}
  });

  renderUsers();
  connect();
})();