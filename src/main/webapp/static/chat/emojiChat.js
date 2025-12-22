(() => {
  const statusEl = document.querySelector("#emoji-ws-status");

  const leftCard = document.querySelector("#p1");
  const rightCard = document.querySelector("#p2");

  const leftNameEl = leftCard?.querySelector(".name");
  const rightNameEl = rightCard?.querySelector(".name");

  const leftBubble = leftCard?.querySelector(".bubble");
  const rightBubble = rightCard?.querySelector(".bubble");

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

  /* 초기 내 닉네임(세션) */
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

  /*서버가 이모지 보내면: 보낸 사람 카드 위에만 띄우기
   payload = { from, fromNick, emoji } */
  window.onEmojiChat = (payload) => {
    if (!payload) return;

    const meId = window.loginUserId;
    const from = payload.from;
    const fromNick = payload.fromNick;
    const key = payload.emoji;

    const emoji = EMOJI_MAP[key] || key;

    /* 혹시 닉네임이 같이 오면 즉시 반영(상대가 먼저 이모지 보내도 이름 뜨게) */
    if (fromNick) {
      if (meId && from === meId) setMyName(fromNick);
      else setOppName(fromNick);
    }

    if (!meId) {
      /* 로그인 정보가 없으면 디버깅용으로 둘 다 띄우지 말고 오른쪽만 */
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

    /* 오른쪽 플레이어 카드 = 나 */
    const emoji = EMOJI_MAP[key] || key;
    showBubble(myBubble(), emoji);

    /* 서버로 전송 */
    ws.send("EMOJI_CHAT:" + key);
  }

  btns.forEach((b) => {
    b.addEventListener("click", () => sendEmoji(b.dataset.emoji));
  });

  setStatus("EMOJI: 준비됨");
})();
