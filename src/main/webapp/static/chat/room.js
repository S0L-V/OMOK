document.addEventListener("DOMContentLoaded", () => {
  connectWs(contextPath, roomId);

  document.getElementById("sendChatBtn").onclick = sendChat;

  document.querySelectorAll("[data-emoji]").forEach(btn => {
    btn.onclick = () => sendWs("EMOJI:" + btn.dataset.emoji);
  });
});

function sendChat() {
  const input = document.querySelector(".content");
  if (!input.value.trim()) return;
  sendWs("ROOM_CHAT:" + input.value);
  input.value = "";
}

function onWsMessage(msg) {
  if (msg.startsWith("ROOM_CHAT:")) {
    const [, nick, text] = msg.split(":");
    appendLine(`${nick} : ${text}`);
  }

  if (msg.startsWith("EMOJI:")) {
    const [, nick, code] = msg.split(":");
    showBubble(nick, emojiToChar(code));
  }
}

function onStateMessage(state) {
  if (state === "GAME_START") toggleUI(true);
  if (state === "ROOM_CHAT" || state.endsWith("GAMEOVER")) toggleUI(false);
}

function toggleUI(isGame) {
  document.getElementById("waitArea").classList.toggle("hidden", isGame);
  document.getElementById("gameArea").classList.toggle("hidden", !isGame);
  document.getElementById("title").innerText = isGame ? "게임 중" : "대기방";
}

function appendLine(text) {
  const div = document.createElement("div");
  div.innerText = text;
  document.querySelector(".msgArea").appendChild(div);
}

function emojiToChar(code) {
  return { smile:"🙂", angry:"😡", clap:"👏" }[code] || "🙂";
}

function showBubble(nick, emoji) {
  const bubble = document.querySelector(`[data-user='${nick}'] .bubble`);
  if (!bubble) return;
  bubble.innerText = emoji;
  bubble.style.display = "inline";
  setTimeout(() => bubble.style.display = "none", 2000);
}
