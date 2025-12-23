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

	// roomId -> sessions
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
		if (userId == null) {
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

		/* 방 세션 set 가져오기 */
		CopyOnWriteArraySet<Session> set = roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());

		/* 기존에 방에 있던 유저 정보를 새로 들어온 session에게 먼저 보내기 */
		for (Session s : set) {
			if (s == null || !s.isOpen())
				continue;

			String existUserId = (String)s.getUserProperties().get("userId");
			String existNick = (String)s.getUserProperties().get("nickname");
			if (existUserId == null)
				continue;
			if (existNick == null || existNick.isBlank())
				existNick = existUserId;

			sendSingleUser(session, existUserId, existNick);
		}

		/*이제 현재 session을 set에 추가 */
		set.add(session);

		/* 새로 들어온 유저 정보를 방 전체에 알리기 (상대들도 닉네임 갱신) */
		broadcastSingleUser(roomId, userId, nickname);

		System.out.println("[SingleWS] CONNECT session=" + session.getId()
			+ " userId=" + userId + " nick=" + nickname + " roomId=" + roomId);

		/* 게임 서비스 연결 */
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
		if (nickname == null || nickname.isBlank())
			nickname = userId;

		/* 문자열 포맷 이모지: EMOJI_CHAT:smile */
		if (msg.startsWith("EMOJI_CHAT:")) {
			String emoji = msg.substring("EMOJI_CHAT:".length()).trim();
			if (!emoji.isEmpty()) {
				broadcast(roomId,
					"{\"type\":\"EMOJI_CHAT\",\"payload\":{\"from\":\"" + escapeJson(userId)
						+ "\",\"fromNick\":\"" + escapeJson(nickname)
						+ "\",\"emoji\":\"" + escapeJson(emoji) + "\"}}");
			}
			return;
		}

		/* JSON 포맷도 허용: {"type":"EMOJI_CHAT","emoji":"smile"} */
		String trimmed = msg.trim();
		if (trimmed.startsWith("{") && trimmed.contains("\"type\"") && trimmed.contains("EMOJI_CHAT")) {
			String type = extractJsonString(trimmed, "type");
			if ("EMOJI_CHAT".equals(type)) {
				String emoji = extractJsonString(trimmed, "emoji");
				if (emoji != null && !emoji.isBlank()) {
					broadcast(roomId,
						"{\"type\":\"EMOJI_CHAT\",\"payload\":{\"from\":\"" + escapeJson(userId)
							+ "\",\"fromNick\":\"" + escapeJson(nickname)
							+ "\",\"emoji\":\"" + escapeJson(emoji.trim()) + "\"}}");
				}
				return;
			}
		}

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

	/* SINGLE_USER 전송(단일 세션) */
	private void sendSingleUser(Session to, String userId, String nickname) {
		if (to == null || !to.isOpen())
			return;
		try {
			to.getBasicRemote().sendText(
				"{\"type\":\"SINGLE_USER\",\"payload\":{\"userId\":\"" + escapeJson(userId)
					+ "\",\"nickname\":\"" + escapeJson(nickname) + "\"}}");
		} catch (IOException ignore) {}
	}

	/* SINGLE_USER 브로드캐스트(방 단위) */
	private void broadcastSingleUser(String roomId, String userId, String nickname) {
		broadcast(roomId,
			"{\"type\":\"SINGLE_USER\",\"payload\":{\"userId\":\"" + escapeJson(userId)
				+ "\",\"nickname\":\"" + escapeJson(nickname) + "\"}}");
	}

	/* 같은 roomId에만 브로드캐스트 */
	private void broadcast(String roomId, String json) {
		CopyOnWriteArraySet<Session> set = roomSessions.get(roomId);
		if (set == null)
			return;

		for (Session s : set) {
			if (s == null || !s.isOpen())
				continue;
			try {
				s.getBasicRemote().sendText(json);
			} catch (IOException e) {
				try {
					s.close();
				} catch (Exception ignore) {}
				set.remove(s);
			}
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

	/* "key":"value" 형태만 뽑음 */
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