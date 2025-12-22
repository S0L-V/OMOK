(() => {
  const statusEl = document.querySelector("#ws-status");
  const p1Bubble = document.querySelector("#p1 .bubble");
  const p2Bubble = document.querySelector("#p2 .bubble");

  let myColor = null; // 1 or 2

  /* 새로고침해도 유지되는 클라이언트 식별자 */
  const KEY = "omok_client_id";
  let clientId = localStorage.getItem(KEY);
  if (!clientId) {
    clientId = crypto.randomUUID();
    localStorage.setItem(KEY, clientId);
  }

  const EMOJI_MAP = {
    smile: "🙂",
    angry: "😡",
    clap: "👏"
  };

  let ws;

  function setStatus(t) {
    if (statusEl) statusEl.textContent = t;
  }

  function wsUrl() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    const ctx = window.contextPath || "";
    return protocol + location.host + ctx + "/single" + location.search;
  }

  function showBubble(color, emoji) {
    const bubble = (color === 1) ? p1Bubble : p2Bubble;
    if (!bubble) return;

    bubble.textContent = emoji;
    bubble.style.display = "block";

    clearTimeout(bubble._timer);
    bubble._timer = setTimeout(() => {
      bubble.style.display = "none";
      bubble.textContent = "";
    }, 1500);
  }

  /* 이모지 버튼에서 호출 */
  window.sendEmoji = (key) => {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;

    const emoji = EMOJI_MAP[key] || key;
    if (myColor) showBubble(myColor, emoji);

    ws.send(`EMOJI_CHAT:${clientId}|${key}`);
  };

  function connect() {
    setStatus("WS: 연결 중...");
    ws = new WebSocket(wsUrl());

    ws.onopen = () => setStatus("WS: 연결됨");

    ws.onmessage = (e) => {
      let msg;
      try { msg = JSON.parse(e.data); } catch { return; }

      if (msg.type === "SINGLE_START") {
        myColor = Number(msg.color);
        return;
      }

      if (msg.type === "EMOJI_CHAT") {
        const raw = msg.payload?.emoji || "";
        const [fromId, emojiKey] = raw.split("|", 2);
        const emoji = EMOJI_MAP[emojiKey] || emojiKey;

        if (!myColor) {
          showBubble(1, emoji);
          showBubble(2, emoji);
          return;
        }

        if (fromId === clientId) {
          showBubble(myColor, emoji);
        } else {
          showBubble(myColor === 1 ? 2 : 1, emoji);
        }
      }
    };

    ws.onclose = () => setStatus("WS: 종료됨");
    ws.onerror = () => setStatus("WS: 에러");
  }

  window.addEventListener("beforeunload", () => {
    try { ws?.close(); } catch {}
  });

  connect();
})();