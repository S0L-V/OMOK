(() => {
  let lobbyWs = null;

  function buildLobbyWsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    return protocol + location.host + "/ws/lobby";
  }

  function connectLobby() {
    const url = buildLobbyWsUrl();
    console.log("[LobbyWS] connecting:", url);

    lobbyWs = new WebSocket(url);

    lobbyWs.onopen = () => {
      console.log("[LobbyWS] open");
      sendLobby({ type: "LOBBY_ENTER", payload: {} });
    };

    lobbyWs.onmessage = (event) => {
      const msg = safeJsonParse(event.data);
      if (!msg) return;
	  const type = String(msg.type || "").trim();
      switch (msg.type) {
        case "CONNECTED":
          console.log("[LobbyWS] CONNECTED", msg.payload);
          break;

        case "ROOM_LIST": {
          const rooms = msg.payload?.rooms || [];
          renderRoomList(rooms);
          break;
        }

        case "ERROR":
          console.warn("[LobbyWS] ERROR:", msg.payload);
          alert(msg.payload?.message || "오류가 발생했습니다.");
          break;

        default:
          console.log("[LobbyWS] message:", msg);
      }
    };

    lobbyWs.onclose = (event) => console.warn("[LobbyWS] close:", event.code, event.reason);
    lobbyWs.onerror = (event) => console.warn("[LobbyWS] error:", event);
  }

  function sendLobby(obj) {
    if (!lobbyWs || lobbyWs.readyState !== WebSocket.OPEN) return;
    lobbyWs.send(JSON.stringify(obj));
  }

  function safeJsonParse(raw) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  function renderRoomList(rooms) {
    const tbody = document.querySelector("#room-tbody");
    if (!tbody) return;

    if (!rooms.length) {
      tbody.innerHTML = `<tr><td colspan="5">현재 생성된 방이 없습니다.</td></tr>`;
      return;
    }

    tbody.innerHTML = rooms.map(toRowHtml).join("");

    tbody.querySelectorAll(".enter-room-form").forEach((form) => {
      form.addEventListener("submit", (e) => {
        if (!IS_LOGIN) {
          e.preventDefault();
          alert("로그인이 필요합니다.");
        }
      });
    });
  }

  function toRowHtml(r) {
    const roomId = r.id ?? "";
    const roomName = r.roomName ?? "-";
    const isPublic = (String(r.isPublic) === "1") ? "공개" : "비공개 🔒";
    const playType = (String(r.playType) === "0") ? "개인전" : "팀전";
    const current = r.currentUserCnt ?? 0;
    const total = r.totalUserCnt ?? 0;
    

    const disabledAttr = IS_LOGIN ? "" : "disabled";
    const titleAttr = IS_LOGIN ? "" : `title="로그인이 필요합니다."`;

    return `
      <tr>
        <td>${escapeHtml(roomName)}</td>
        <td>${isPublic}</td>
        <td>${playType}</td>
        <td>${current} / ${total}</td>
        <td>
          <form class="enter-room-form" action="${CTX}/room/enter"" method="post">
            <input type="hidden" name="playType" value="${escapeHtml(r.playType)}" />	
			<input type="hidden" name="roomId" value="${escapeHtml(roomId)}" />	
            <button type="submit" ${disabledAttr} ${titleAttr}>입장</button>
          </form>
        </td>
      </tr>
    `;
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
    try { lobbyWs?.close(); } catch (_) {}
  });

  // 일반 로그인 처리
  const btnLogin = document.getElementById("btn-login");
  const emailInput = document.getElementById("login-email");
  const passwordInput = document.getElementById("login-password");

  if (btnLogin && emailInput && passwordInput) {
    btnLogin.addEventListener("click", async () => {
      const email = emailInput.value.trim();
      const password = passwordInput.value.trim();

      if (!email || !password) {
        alert("이메일과 비밀번호를 입력해주세요.");
        return;
      }

      try {
        const response = await fetch(CTX + "/noamlLogin", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok && data.accessToken) {
          alert("로그인 성공!");
          location.reload();
        } else {
          alert(data.message || "로그인 실패");
        }
      } catch (error) {
        console.error("로그인 오류:", error);
        alert("로그인 중 오류가 발생했습니다.");
      }
    });

    // Enter 키로 로그인
    passwordInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") {
        btnLogin.click();
      }
    });
  }

  connectLobby();
})();
