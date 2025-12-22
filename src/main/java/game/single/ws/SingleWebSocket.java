package game.single.ws;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import config.WebSocketConfig;
import game.single.dao.SinglePlayerDAO;
import game.single.dao.SinglePlayerDAOImpl;
import game.single.service.SingleGameServiceImpl;
import game.single.service.SingleGameServiceManager;
import session.SessionContext;

@ServerEndpoint(value = "/single", configurator = WebSocketConfig.class)
public class SingleWebSocket {

	private static final SingleGameServiceManager manager = SingleGameServiceManager.getInstance();
	private static final SinglePlayerDAO singlePlayerDao = new SinglePlayerDAOImpl();
	private static final SessionContext sessionContext = SessionContext.getInstance();

	// roomId -> sessions (같은 방에만 브로드캐스트)
	private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> roomSessions = new ConcurrentHashMap<>();

	private String getRoomId(Session session) {
		List<String> ids = session.getRequestParameterMap().get("roomId");
		return (ids == null || ids.isEmpty()) ? null : ids.get(0);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws Exception {
		String roomId = getRoomId(session);
		if (roomId == null || roomId.isBlank()) {
			session.close();
			return;
		}

		HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession == null) {
			session.close();
			return;
		}

		sessionContext.connectSession(session, httpSession);

		String userId = sessionContext.getUserId(session);
		if (userId == null || userId.isBlank()) {
			session.close();
			return;
		}

		// 방 멤버 검증
		if (!singlePlayerDao.isMember(roomId, userId)) {
			session.close();
			return;
		}

		// 닉네임
		String nickname = (String)httpSession.getAttribute("loginNickname");
		if (nickname == null || nickname.isBlank())
			nickname = userId;

		session.getUserProperties().put("roomId", roomId);
		session.getUserProperties().put("userId", userId);
		session.getUserProperties().put("nickname", nickname);

		// 방 세션 등록
		CopyOnWriteArraySet<Session> set = roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
		set.add(session);

		System.out.println("[SingleWS] CONNECT session=" + session.getId()
			+ " userId=" + userId + " nick=" + nickname + " roomId=" + roomId);

		// ✅ 1) 새로 들어온 사람에게: 기존 유저들(=이미 들어와 있던 상대) 목록을 먼저 보냄
		sendExistingUsersTo(session, roomId);

		// ✅ 2) 그리고 이 사람 정보를: 방 전체(나 포함)에 브로드캐스트
		broadcast(roomId, singleUserJson(userId, nickname));

		// 게임 서비스 연결
		SingleGameServiceImpl service = manager.getOrCreate(roomId);
		service.onOpen(session, userId);
	}

	@OnMessage
	public void onMessage(String msg, Session session) throws Exception {
		if (msg == null)
			return;

		String roomId = (String)session.getUserProperties().get("roomId");
		String userId = (String)session.getUserProperties().get("userId");
		String nickname = (String)session.getUserProperties().get("nickname");
		if (roomId == null || userId == null)
			return;

		// 문자열 포맷 이모지: "EMOJI_CHAT:smile"
		if (msg.startsWith("EMOJI_CHAT:")) {
			String emoji = msg.substring("EMOJI_CHAT:".length()).trim();
			if (!emoji.isEmpty()) {
				broadcast(roomId, emojiJson(userId, nickname, emoji));
			}
			return;
		}

		// JSON 포맷도 허용: {"type":"EMOJI_CHAT","emoji":"smile"}
		String trimmed = msg.trim();
		if (trimmed.startsWith("{") && trimmed.contains("\"type\"") && trimmed.contains("EMOJI_CHAT")) {
			String type = extractJsonString(trimmed, "type");
			if ("EMOJI_CHAT".equals(type)) {
				String emoji = extractJsonString(trimmed, "emoji");
				if (emoji != null && !emoji.isBlank()) {
					broadcast(roomId, emojiJson(userId, nickname, emoji.trim()));
				}
				return;
			}
		}

		// 나머지는 게임 로직
		SingleGameServiceImpl service = manager.getOrCreate(roomId);
		service.onMessage(msg, session);
	}

	@OnClose
	public void onClose(Session session) {
		String roomId = (String)session.getUserProperties().get("roomId");
		if (roomId != null) {
			CopyOnWriteArraySet<Session> set = roomSessions.get(roomId);
			if (set != null) {
				set.remove(session);
				if (set.isEmpty())
					roomSessions.remove(roomId);
			}

			SingleGameServiceImpl service = manager.getOrCreate(roomId);
			service.onClose(session);
		}
	}

	// ===== helpers =====

	private void sendExistingUsersTo(Session target, String roomId) {
		CopyOnWriteArraySet<Session> set = roomSessions.get(roomId);
		if (set == null)
			return;

		for (Session s : set) {
			if (s == null || !s.isOpen())
				continue;

			String uid = (String)s.getUserProperties().get("userId");
			String nick = (String)s.getUserProperties().get("nickname");
			if (uid == null)
				continue;
			if (nick == null || nick.isBlank())
				nick = uid;

			sendTo(target, singleUserJson(uid, nick));
		}
	}

	private String singleUserJson(String userId, String nickname) {
		return "{\"type\":\"SINGLE_USER\",\"payload\":{\"userId\":\"" + escapeJson(userId)
			+ "\",\"nickname\":\"" + escapeJson(nickname) + "\"}}";
	}

	private String emojiJson(String userId, String nickname, String emojiKey) {
		return "{\"type\":\"EMOJI_CHAT\",\"payload\":{\"from\":\"" + escapeJson(userId)
			+ "\",\"fromNick\":\"" + escapeJson(nickname)
			+ "\",\"emoji\":\"" + escapeJson(emojiKey) + "\"}}";
	}

	private void broadcast(String roomId, String json) {
		CopyOnWriteArraySet<Session> set = roomSessions.get(roomId);
		if (set == null)
			return;

		for (Session s : set) {
			if (s == null || !s.isOpen())
				continue;
			sendTo(s, json);
		}
	}

	private void sendTo(Session s, String json) {
		try {
			s.getBasicRemote().sendText(json);
		} catch (IOException e) {
			try {
				s.close();
			} catch (Exception ignore) {}
		}
	}

	private static String escapeJson(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r");
	}

	// 아주 단순한 "key":"value"만 파싱
	private static String extractJsonString(String json, String key) {
		String pattern = "\"" + key + "\"";
		int i = json.indexOf(pattern);
		if (i < 0)
			return null;

		int colon = json.indexOf(":", i + pattern.length());
		if (colon < 0)
			return null;

		int q1 = json.indexOf("\"", colon + 1);
		if (q1 < 0)
			return null;

		int q2 = json.indexOf("\"", q1 + 1);
		if (q2 < 0)
			return null;

		return json.substring(q1 + 1, q2);
	}
}