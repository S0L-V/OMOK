package game.single.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import game.single.service.SingleGameServiceImpl;

@ServerEndpoint("/omok")
public class SingleWebSocket {

	private static SingleGameServiceImpl service = SingleGameServiceImpl.getInstance();
	// 이모티콘 채팅 브로드캐스트용 세션 목록
	private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session) throws Exception {
		sessions.add(session);
		service.onOpen(session);
	}

	@OnMessage
	public void onMessage(String msg, Session session) throws Exception {
		/* 이모티콘 채팅 메시지면 게임로직으로 안 넘기고 브로드캐스트 */
		/* 예시: EMOJI_CHAT:😀   또는  EMOJI_CHAT:heart */
		if (msg != null && msg.startsWith("EMOJI_CHAT:")) {
			String emoji = msg.substring("EMOJI_CHAT:".length()); // ":" 뒤
			emoji = emoji == null ? "" : emoji.trim();

			if (!emoji.isEmpty()) {
				broadcast("{\"type\":\"EMOJI_CHAT\",\"payload\":{\"emoji\":\"" + escapeJson(emoji) + "\"}}");
			}
			return;
		}
		service.onMessage(msg, session); // 나머지는 기존 게임 로직으로
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
		service.onClose(session);
	}

	private void broadcast(String json) {
		for (Session s : sessions) {
			if (s == null || !s.isOpen())
				continue;
			try {
				s.getBasicRemote().sendText(json);
			} catch (IOException e) {
				// 보내기 실패하면 세션 제거
				try {
					s.close();
				} catch (Exception ignore) {}
				sessions.remove(s);
			}
		}
	}

	/* JSON 문자열 처리 (따옴표/역슬래시/개행) */
	private static String escapeJson(String s) {
		return s
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r");
	}
}
