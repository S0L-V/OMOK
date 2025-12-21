(() => {
  const statusEl = document.querySelector("#ws-status");
  const p1Bubble = document.querySelector("#p1 .bubble");
  const p2Bubble = document.querySelector("#p2 .bubble");

  /* 서버에서 SINGLE_START로 내려주는 내 돌 색(1 or 2) */
  let myColor = null;

  /* 상대와 구분용 식별자 -> 새로고침해도 유지 */
  const CLIENT_ID_KEY = "omok_client_id";
  let clientId = localStorage.getItem(CLIENT_ID_KEY);
  if (!clientId) {
    clientId = crypto?.randomUUID ? crypto.randomUUID() : String(Date.now()) + "_" + Math.random();
    localStorage.setItem(CLIENT_ID_KEY, clientId);
  }

  const EMOJI_MAP = {
    smile: "🙂",
    angry: "😡",
    clap: "👏",
  };

  let ws = null;

  function setStatus(t) {
    if (statusEl) statusEl.textContent = t;
  }

  function wsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    const ctx = window.contextPath || "";
    /* @ServerEndpoint("/omok") 는 "컨텍스트 경로 + /omok" */
    return protocol + location.host + ctx + "/omok";
  }

  function safeJson(raw) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  function showBubble(color, emojiChar) {
    const bubble = (String(color) === "1") ? p1Bubble : p2Bubble;
    if (!bubble) return;

    bubble.textContent = emojiChar;
    bubble.style.display = "inline-block";

    /* 1.5초 후 자동 숨김 */
    window.clearTimeout(bubble._t);
    bubble._t = window.setTimeout(() => {
      bubble.style.display = "none";
      bubble.textContent = "";
    }, 1500);
  }

  /* 전역 함수 */
  window.sendEmoji = (emojiKey) => {
    const emojiChar = EMOJI_MAP[emojiKey] || emojiKey;
    if (!ws || ws.readyState !== WebSocket.OPEN) return;

    /* 내 화면에는 즉시 띄우고 서버에도 전송 */
    if (myColor) showBubble(myColor, emojiChar);

    /* 서버는 문자열만 받고 다시 브로드캐스트 하니까
    clientId|emojiKey 로 보내서 수신 시 누가 보냈는지 구분 */
    const payload = `${clientId}|${emojiKey}`;
    ws.send(`EMOJI_CHAT:${payload}`);
  };

  function connect() {
    setStatus("WS: 연결 중...");
    ws = new WebSocket(wsUrl());

    ws.onopen = () => {
      setStatus("WS: 연결됨");
    };

    ws.onmessage = (e) => {
      const msg = safeJson(e.data);
      if (!msg || !msg.type) return;

      /* 게임 시작 시 내 색 받기 */
      if (msg.type === "SINGLE_START") {
        myColor = Number(msg.color) || null; // 1 or 2
        return;
      }

      /* 이모티콘 채팅 수신 */
      if (msg.type === "EMOJI_CHAT") {
        const raw = msg.payload?.emoji ?? "";
        const [fromId, emojiKey] = String(raw).split("|", 2);

        const emojiChar = EMOJI_MAP[emojiKey] || emojiKey || "🙂";

        /* 내 clientId면 내 말풍선, 아니면 상대 말풍선 */
        if (fromId && fromId === clientId) {
          if (myColor) showBubble(myColor, emojiChar);
          else { showBubble(1, emojiChar); showBubble(2, emojiChar); }
        } else {
          if (myColor) {
            const other = (myColor === 1) ? 2 : 1;
            showBubble(other, emojiChar);
          } else {
            /* 색 모르면 일단 둘 다 보여주기 */
            showBubble(1, emojiChar);
            showBubble(2, emojiChar);
          }
        }
        return;
      }

      /* 나머지 게임 메시지는 기존 게임 로직 파일이 따로 있으면 거기서 처리 여긴 채팅 UI만 붙이는 파일 */
    };

    ws.onclose = () => setStatus("WS: 종료됨");
    ws.onerror = () => setStatus("WS: 에러");
  }

  window.addEventListener("beforeunload", () => {
    try { ws?.close(); } catch (_) {}
  });

  connect();
})();