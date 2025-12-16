(function initGameChat() {
  if (typeof contextPath === "undefined" || !contextPath) {
    window.contextPath = "/" + location.pathname.split("/")[1];
  }
  if (typeof roomId === "undefined" || !roomId) {
    const params = new URLSearchParams(location.search);
    window.roomId = params.get("roomId") || "lobby";
  }

  document.addEventListener("DOMContentLoaded", function () {
    connectWs(window.contextPath, window.roomId);
  });
})();

function sendEmoji(code) {
  sendWs("EMOJI:" + code);
}

function onWsMessage(msg) {
  if (msg.indexOf("EMOJI:") === 0) {
    const parts = msg.split(":");
    const nick = (parts.length > 1 && parts[1] != null) ? parts[1] : "";
    const code = (parts.length > 2 && parts[2] != null) ? parts[2] : "";
    showBubble(nick, emojiToChar(code));
    return;
  }

  // 게임 중에는 채팅 표시 안 함
  console.log("GAME MSG:", msg);
}

function onStateMessage(phase) {
  // 사람이 줄어서 다시 대기실 상태가 되면 방으로 돌아감
  if (phase === "LOBBY") {
    location.href =
      window.contextPath +
      "/roomChat.jsp?roomId=" +
      encodeURIComponent(window.roomId);
  }
}

function emojiToChar(code) {
  const map = { smile: "🙂", angry: "😡", clap: "👏" };
  return map[code] || "🙂";
}

function showBubble(nick, emojiChar) {
  // gameChat.jsp에서 data-user="Player1", "Player2" 로 맞추면 정확히 매칭됨
  let bubble = document.querySelector("[data-user='" + nick.replace(/'/g, "\\'") + "'] .bubble");

  // 못 찾으면 첫번째 플레이어에 표시
  if (!bubble) bubble = document.querySelector(".player .bubble");
  if (!bubble) return;

  bubble.innerText = emojiChar;
  bubble.style.display = "inline-block";

  // 너무 빨리 사라지면 2500ms 정도로 
  setTimeout(function () {
    bubble.style.display = "none";
  }, 2500);
}
