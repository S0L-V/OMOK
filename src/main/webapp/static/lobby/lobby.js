(() => {
  let ws;

  function wsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws";
  }

  function connect() {
    const url = wsUrl();
    console.log("[WS] connecting:", url);

    ws = new WebSocket(url);

    ws.onopen = () => {
      console.log("[WS] open");
      send({ type: "LOBBY_ENTER" });
    };

    ws.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;

      switch (msg.type) {
        case "CONNECTED":
          console.log("[WS] CONNECTED");
          break;

        case "INIT_ROOM_LIST": // 초기 목록
          renderRoomList(msg.rooms || []);
          break;

        case "ROOM_LIST_UPDATE": // 목록 업데이트 발생
          renderRoomList(msg.rooms || []);
          break;

        case "ERROR":
          console.warn("[WS] ERROR:", msg.code, msg.message);
          alert(msg.message || "오류가 발생했습니다.");
          break;

        default:
          console.log("[WS] message:", msg);
      }
    };

    ws.onclose = (event) => {
      console.warn("[WS] close:", event.code, event.reason);
    };

    ws.onerror = (event) => {
      console.warn("[WS] error:", event);
    };
  }

  function send(obj) {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(JSON.stringify(obj));
  }

  function safeJsonParse(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      console.warn("[WS] invalid json:", raw);
      return null;
    }
  }

  function renderRoomList(rooms) {
    const tbody = document.querySelector("#room-tbody");
    if (!tbody) {
      console.warn("room tbody(#room-tbody) not found");
      return;
    }

    if (!rooms.length) {
      tbody.innerHTML = `<tr><td colspan="5">현재 생성된 방이 없습니다.</td></tr>`;
      return;
    }

    tbody.innerHTML = rooms.map(toRowHtml).join("");

    tbody.querySelectorAll(".btn-join").forEach((btn) => {
      btn.addEventListener("click", () => {
        const roomId = btn.getAttribute("data-room-id");
        if (!roomId) return;
        
      	send({ type: "ROOM_ENTER", roomId });

        location.href = `/room?roomId=${encodeURIComponent(roomId)}`;
      });
    });
  }

  function toRowHtml(r) {
    const roomId = r.roomId ?? r.id ?? "";
    const roomName = r.roomName ?? "-";
    const isPublic = normalizePublic(r);
    const playType = normalizePlayType(r);
    const { current, total } = normalizeCount(r);

    return `
      <tr>
        <td>${escapeHtml(roomName)}</td>
        <td>${isPublic}</td>
        <td>${escapeHtml(playType)}</td>
        <td>${current}/${total}</td>
        <td>
          <button data-room-id="${escapeHtml(roomId)}" class="btn-join">입장</button>
        </td>
      </tr>
    `;
  }

  function normalizePublic(r) {
    const v = r.isPublic ?? r.public ?? r.open;
    if (v === "0") return "공개";
    return "비공개";
  }

  function normalizePlayType(r) {
    const v = r.playType ?? r.gameType ?? "-";
    if (v === "0") return "개인전";
    if (v === "1") return "팀전";
    return String(v);
  }

  function normalizeCount(r) {
    const current = r.currentUserCnt ?? r.currentUserCount ?? r.current ?? 0;
    const total = r.totalUserCnt ?? r.totalUserCount ?? r.total ?? 0;
    return { current, total };
  }

  function escapeHtml(str) {
    return String(str)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  window.addEventListener("beforeunload", () => {
    try {
      ws?.close();
    } catch (_) {}
  });

  connect();
})();
