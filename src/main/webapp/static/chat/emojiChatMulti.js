(() => {
  const statusEl = document.querySelector("#emoji-ws-status");

  /* 이모지 매핑 */
  const EMOJI_MAP = { smile: "🙂", angry: "😡", clap: "👏" };

  function setStatus(t) {
    if (statusEl) statusEl.textContent = t;
  }

  /* slot으로 카드 찾기 */
  function getCardBySlot(slot) {
    return document.querySelector(`.player-card[data-slot='${slot}']`);
  }

  /* slot 카드에 이모지 버블 표시 */
  function showBubbleOnSlot(slot, emoji) {
    const card = getCardBySlot(slot);
    if (!card) return;

    const bubble = card.querySelector(".bubble");
    if (!bubble) return;

    bubble.textContent = emoji;
    bubble.style.display = "block";

    clearTimeout(bubble._t);
    bubble._t = setTimeout(() => {
      bubble.style.display = "none";
      bubble.textContent = "";
    }, 1200);
  }

  /* 
   * 서버 → 이모지 수신
   * payload = { slot, emoji, from?, fromNick? }
   */
  window.onEmojiChat = (payload) => {
    if (!payload) return;

    const slot = payload.slot; // 서버와 키 일치
    const key = payload.emoji;
    const emoji = EMOJI_MAP[key] || key;

    if (typeof slot !== "number") return;

    showBubbleOnSlot(slot, emoji);
  };

  /* 
   * 서버 → 슬롯/닉네임 수신
   * payload = { slot, userId, nickname }
   */
  window.onMultiUser = (payload) => {
    if (!payload) return;

    const slot = payload.slot;
    const nickname = payload.nickname;

    if (typeof slot !== "number") return;

    const card = getCardBySlot(slot);
    if (!card) return;

    const nameEl = card.querySelector(".name");
    if (nameEl && nickname) {
      nameEl.textContent = nickname;
    }
  };

  /* 이모지 전송 */
  function sendEmoji(key) {
    const ws = window.singleWs; // multi에서도 동일 소켓 변수 사용
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      setStatus("EMOJI: WS 미연결");
      return;
    }

    const emoji = EMOJI_MAP[key] || key;

    /* 내 슬롯 카드에 즉시 표시 */
    if (typeof window.mySlot === "number") {
      showBubbleOnSlot(window.mySlot, emoji);
    }

    /* 서버는 문자열 프로토콜을 기대함 */
    ws.send("EMOJI_CHAT:" + key);
  }

  /* 외부에서 호출 가능하도록 노출 */
  window.sendEmoji = sendEmoji;

  /* 버튼 바인딩 */
  document
    .querySelectorAll(".emoji-buttons button[data-emoji]")
    .forEach((b) => {
      b.addEventListener("click", () => {
        sendEmoji(b.dataset.emoji);
      });
    });

  setStatus("EMOJI: 준비됨");
})();
