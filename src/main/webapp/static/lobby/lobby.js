(() => {
  let lobbyWs = null;

 function buildLobbyWsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws";
  }

  /* =========================
   * Connect
   * ========================= */
  function connectLobby() {
    const url = buildLobbyWsUrl();
    console.log("[LobbyWS] connecting:", url);

    lobbyWs = new WebSocket(url);

    lobbyWs.onopen = () => {
      console.log("[LobbyWS] open");
      sendLobby({ type: "LOBBY_ENTER" });
    };

    lobbyWs.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;

      switch (msg.type) {
        case "CONNECTED":
          console.log("[LobbyWS] CONNECTED");
          break;

        case "INIT_ROOM_LIST":
          renderRoomList(msg.rooms || []);
          break;

        case "ROOM_LIST_UPDATE":
          renderRoomList(msg.rooms || []);
          break;

        case "ERROR":
          console.warn("[LobbyWS] ERROR:", msg.code, msg.message);
          alert(msg.message || "오류가 발생했습니다.");
          break;

        default:
          console.log("[LobbyWS] message:", msg);
      }
    };

    lobbyWs.onclose = (event) => {
      console.warn("[LobbyWS] close:", event.code, event.reason);
    };

    lobbyWs.onerror = (event) => {
      console.warn("[LobbyWS] error:", event);
    };
  }

  /* =========================
   * Send
   * ========================= */
  function sendLobby(obj) {
    if (!lobbyWs || lobbyWs.readyState !== WebSocket.OPEN) return;
    lobbyWs.send(JSON.stringify(obj));
  }

  /* =========================
   * Utils
   * ========================= */
  function safeJsonParse(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      console.warn("[LobbyWS] invalid json:", raw);
      return null;
    }
  }

  /* =========================
   * UI
   * ========================= */
  function renderRoomList(rooms) {
    const tbody = document.querySelector("#room-tbody");
    if (!tbody) return;

    if (!rooms.length) {
      tbody.innerHTML = `<tr><td colspan="5">현재 생성된 방이 없습니다.</td></tr>`;
      return;
    }

    tbody.innerHTML = rooms.map(toRowHtml).join("");

    tbody.querySelectorAll(".btn-join").forEach((btn) => {
      btn.addEventListener("click", () => {
        const roomId = btn.dataset.roomId;
        if (!roomId) return;

        location.href = `/room?roomId=${encodeURIComponent(roomId)}`;
      });
    });
  }

  function toRowHtml(r) {
    const roomId = r.roomId ?? "";
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
          <button class="btn-join" data-room-id="${escapeHtml(roomId)}">
            입장
          </button>
        </td>
      </tr>
    `;
  }

  function normalizePublic(r) {
    return r.isPublic === "0" ? "공개" : "비공개";
  }

  function normalizePlayType(r) {
    if (r.playType === "0") return "개인전";
    if (r.playType === "1") return "팀전";
    return "-";
  }

  function normalizeCount(r) {
    return {
      current: r.currentUserCnt ?? 0,
      total: r.totalUserCnt ?? 0,
    };
  }

  function escapeHtml(str) {
    return String(str)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  /* =========================
   * Cleanup
   * ========================= */
  window.addEventListener("beforeunload", () => {
    try {
      lobbyWs?.close();
    } catch (_) {}
  });

  /* =========================
   * Start
   * ========================= */
  connectLobby();
})();
