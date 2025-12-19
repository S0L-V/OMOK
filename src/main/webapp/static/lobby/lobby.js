// /static/lobby/lobby.js

(() => {
  let ws = null;

  function buildWsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws";
  }

  function connect() {
    const url = buildWsUrl();
    ws = new WebSocket(url);

    ws.onopen = () => {
      console.log("[WS] open:", url);
    };

    ws.onmessage = (event) => {
      let msg;
      try {
        msg = JSON.parse(event.data);
      } catch (e) {
        console.warn("[WS] invalid json:", event.data);
        return;
      }

      switch (msg.type) {
        case "CONNECTED": {
          console.log("[WS] CONNECTED");

          send({ type: "LOBBY_ENETER" });

          send({ type: "ROOM_LIST_REQUEST" });
          break;
        }

        case "ROOM_LIST": {
          renderRoomList(msg.rooms || []);
          break;
        }

        case "ROOM_LIST_UPDATE": {
          renderRoomList(msg.rooms || []);
          break;
        }

        case "ERROR": {
          console.warn("[WS] ERROR:", msg.code, msg.message);
          alert(msg.message || "오류가 발생했습니다.");
          break;
        }

        default: {
          console.log("[WS] message:", msg);
        }
      }
    };

    ws.onclose = (event) => {
      console.log("[WS] close:", event.code, event.reason);
    };

    ws.onerror = (event) => {
      console.warn("[WS] error:", event);
    };
  }

  function send(obj) {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(JSON.stringify(obj));
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

    tbody.innerHTML = rooms
      .map((r) => {
        const roomId = r.roomId || r.id;
        const roomName = r.roomName ?? "-";
        const isPublic = r.isPublic ? "공개" : "비공개";
        const playType = r.playType ?? "-";
        const current = r.currentUserCnt ?? r.currentUserCount ?? 0;
        const total = r.totalUserCnt ?? r.totalUserCount ?? 0;

        return `
          <tr>
            <td>${escapeHtml(roomName)}</td>
            <td>${isPublic}</td>
            <td>${escapeHtml(String(playType))}</td>
            <td>${current}/${total}</td>
            <td>
              <button data-room-id="${roomId}" class="btn-join">입장</button>
            </td>
          </tr>
        `;
      })
      .join("");

    tbody.querySelectorAll(".btn-join").forEach((btn) => {
      btn.addEventListener("click", () => {
        const roomId = btn.getAttribute("data-room-id");
        send({ type: "ROOM_JOIN", roomId });
      });
    });
  }

  function escapeHtml(str) {
    return str
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  // 페이지 나갈 때 정리(선택)
  window.addEventListener("beforeunload", () => {
    try {
      // 명시적으로 leave를 보내고 싶으면:
      send({ type: "LOBBY_EXIT" });
      ws?.close();
    } catch (_) {}
  });

  // 시작
  connect();
})();
