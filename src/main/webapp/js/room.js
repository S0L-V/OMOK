(function initRoomChat() {
  if (typeof contextPath === "undefined" || !contextPath) {
    window.contextPath = "/" + location.pathname.split("/")[1];
  }

  if (typeof roomId === "undefined" || !roomId) {
    const params = new URLSearchParams(location.search);
    window.roomId = params.get("roomId") || "lobby";
  }

  document.addEventListener("DOMContentLoaded", function () {
    connectWs(window.contextPath, window.roomId);

    const input = document.querySelector(".content");
    if (input) {
      input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
			e.preventDefault();
			sendChat();
			}
      });
    }
  });
})();

function sendChat() {
  const input = document.querySelector(".content");
  if (!input) return;

  const text = input.value.trim();
  if (text.length === 0) return;

  sendWs("CHAT:" + text);
  input.value = "";
}

function onWsMessage(msg) {
  if (msg.indexOf("CHAT:") === 0) {
    const parts = msg.split(":");
    const nick = (parts.length > 1 && parts[1] != null) ? parts[1] : "";
    const text = (parts.length > 2) ? parts.slice(2).join(":") : "";
    appendLine(nick + " : " + text);
    return;
  }

  // SYSTEM / EMOJI 등은 일단 그대로 출력
  appendLine(msg);
}

function onStateMessage(phase) {
  // 2명 이상 되면(개인전 기준) 서버가 STATE:IN_GAME을 쏴줌 → 게임 화면으로 이동
  if (phase === "IN_GAME") {
    location.href =
      window.contextPath +
      "/gameChat.jsp?roomId=" +
      encodeURIComponent(window.roomId);
  }
}

function appendLine(text) {
  const box = document.querySelector(".msgArea");
  if (!box) return;

  const div = document.createElement("div");
  div.innerText = text;
  box.appendChild(div);
  box.scrollTop = box.scrollHeight;
}
