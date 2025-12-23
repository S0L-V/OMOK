(() => {
  const statusEl = document.querySelector("#emoji-ws-status");

  const p1 = document.querySelector("#p1"); // 상대
  const p2 = document.querySelector("#p2"); // 나

  const leftNameEl  = p1 ? p1.querySelector(".name") : null;
  const rightNameEl = p2 ? p2.querySelector(".name") : null;
  const leftBubble  = p1 ? p1.querySelector(".bubble") : null;
  const rightBubble = p2 ? p2.querySelector(".bubble") : null;

  const btns = document.querySelectorAll(".emoji-buttons button[data-emoji]");

  const EMOJI_MAP = { smile: "🙂", angry: "😡", clap: "👏" };

  function setStatus(t) {
    if (statusEl) statusEl.textContent = t;
  }

  function showBubble(bubbleEl, emoji) {
    if (!bubbleEl) return;

    bubbleEl.textContent = emoji;
    bubbleEl.style.display = "block";

    clearTimeout(bubbleEl._t);
    bubbleEl._t = setTimeout(() => {
      bubbleEl.style.display = "none";
      bubbleEl.textContent = "";
    }, 1200);
  }

  /* 오른쪽=나, 왼쪽=상대 */
  function myBubble() { return rightBubble; }
  function oppBubble() { return leftBubble; }
  function setMyName(name) { if (rightNameEl && name) rightNameEl.textContent = name; }
  function setOppName(name) { if (leftNameEl && name) leftNameEl.textContent = name; }

  /* 초기 내 닉네임 */
  setMyName(window.loginNickname || "나");

  /* 서버가 접속자 정보 보내면 닉네임 반영
     payload = { userId, nickname } */
  window.onSingleUser = (payload) => {
    if (!payload) return;

    const meId = window.loginUserId;
    const uid = payload.userId;
    const nick = payload.nickname;

    if (!meId || !uid) return;

    if (uid === meId) {
      setMyName(nick || window.loginNickname || "나");
    } else {
      setOppName(nick || "상대");
    }
  };

  /* 서버가 이모지 보내면: 보낸 사람 카드 위에만 띄우기
     payload = { from, fromNick, emoji } */
  window.onEmojiChat = (payload) => {
    if (!payload) return;

    const meId = window.loginUserId;
    const from = payload.from;
    const fromNick = payload.fromNick;
    const key = payload.emoji;

    const emoji = EMOJI_MAP[key] || key;

    if (fromNick) {
      if (meId && from === meId) setMyName(fromNick);
      else setOppName(fromNick);
    }

    if (!meId) {
      showBubble(myBubble(), emoji);
      return;
    }

    if (from === meId) showBubble(myBubble(), emoji);
    else showBubble(oppBubble(), emoji);
  };

  function sendEmoji(key) {
    const ws = window.singleWs;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      setStatus("EMOJI: WS 아직 연결 전");
      return;
    }

    const emoji = EMOJI_MAP[key] || key;

    /* 오른쪽 플레이어 카드 = 나 */
    showBubble(myBubble(), emoji);

    /* 서버로 전송 */
    ws.send("EMOJI_CHAT:" + key);
  }

  window.sendEmoji = sendEmoji;

  btns.forEach((b) => {
    b.addEventListener("click", () => sendEmoji(b.dataset.emoji));
  });

  setStatus("EMOJI: 준비됨");
})();
