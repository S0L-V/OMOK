(() => {
  const statusEl = document.querySelector("#emoji-ws-status");

  const EMOJI_MAP = { smile: "🙂", angry: "😡", clap: "👏" };

  function setStatus(t) {
    if (statusEl) statusEl.textContent = t;
  }

  function getCardBySlot(slot) {
    return document.querySelector(`.player-card[data-slot='${slot}']`);
  }

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
  서버 → 이모지 수신
  payload = { fromSlot, emoji } 
  */
  window.onEmojiChat = (payload) => {
    if (!payload) return;

    const slot = payload.slot;
    const key = payload.emoji;
    const emoji = EMOJI_MAP[key] || key;

    showBubbleOnSlot(slot, emoji);
  };

  function sendEmoji(key) {
    const ws = window.singleWs;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      setStatus("EMOJI: WS 미연결");
      return;
    }

    const emoji = EMOJI_MAP[key] || key;

    /* 내 슬롯 */
    showBubbleOnSlot(window.mySlot, emoji);

    ws.send("EMOJI_CHAT:" + key);
  }

  window.sendEmoji = sendEmoji;

  document.querySelectorAll(".emoji-buttons button[data-emoji]")
    .forEach(b => b.onclick = () => sendEmoji(b.dataset.emoji));

  setStatus("EMOJI: 준비됨");
})();
