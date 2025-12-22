package lobby.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonSyntaxException;

import config.WebSocketConfig;
import session.SessionContext;
import util.Parser;

/**
 * 로비 WebSocket 엔드포인트
 *
 * 모든 메시지는 다음 구조를 따른다.
 * {
 *   "type": "...",
 *   "payload": { ... }
 * }
 *
 * 지원하는 클라이언트 요청(type):
 * - LOBBY_ENTER : 로비 진입(방 목록 전송)
 * - ROOM_LIST   : 방 목록 재요청
 *
 * 서버에서 DB 변경 발생 시 (방 생성/입장/퇴장/삭제 등)
 * broadcastRoomList()를 호출하면 로비 접속자 전체에게 방 목록이 갱신된다.
 */
@ServerEndpoint(value = "/ws/lobby", configurator = WebSocketConfig.class)
public class LobbyWebSocket {

	private static final SessionContext sessionContext = SessionContext.getInstance();
	private static final LobbyWebSocketService service = new LobbyWebSocketService();

	/** 현재 로비에 연결된 WebSocket 세션들 */
	private static final Set<Session> lobbySessions = ConcurrentHashMap.newKeySet();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		HttpSession httpSession = (HttpSession)config.getUserProperties()
			.get(HttpSession.class.getName());

		sessionContext.connectSession(session, httpSession);
		lobbySessions.add(session);

		System.out.println(
			"[LobbyWS] CONNECTED wsSessionId=" + session.getId()
				+ " userId=" + sessionContext.getUserId(session)
				+ " nick=" + sessionContext.getNickname(session)
				+ " role=" + sessionContext.getRole(session));

		// CONNECTED 응답
		Map<String, Object> payload = new HashMap<>();
		payload.put("login", sessionContext.isLogin(session));
		payload.put("userId", sessionContext.getUserId(session));
		payload.put("nickname", sessionContext.getNickname(session));
		payload.put("role", sessionContext.getRole(session).name());

		sendRawIfOpen(session, "CONNECTED", payload);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("[LobbyWS] CLOSE wsSessionId=" + session.getId()
			+ " code=" + reason.getCloseCode()
			+ " reason=" + reason.getReasonPhrase());

		lobbySessions.remove(session);
		sessionContext.disconnectSession(session);
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		System.err.println("[LobbyWS] ERROR wsSessionId=" + (session != null ? session.getId() : "null"));
		thr.printStackTrace();

		if (session != null) {
			lobbySessions.remove(session);
			sessionContext.disconnectSession(session);
		}
	}

	@OnMessage
	public void onMessage(Session session, String text) {
		try {
			Map<String, Object> msg = Parser.parse(text);
			String type = Parser.asString(msg.get("type"));
			Map<String, Object> payload = Parser.asMap(msg.get("payload"));

			if (type == null || type.isBlank()) {
				service.sendIfOpen(session, "ERROR", Map.of(
					"code", "MISSING_TYPE",
					"message", "type이 없습니다."));
				return;
			}

			switch (type) {
				case "LOBBY_ENTER": {
					System.out.println("[LobbyWS] LOBBY_ENTER from " + session.getId());
					service.sendRoomList(session);
					break;
				}

				case "ROOM_LIST": {
					service.sendRoomList(session);
					break;
				}

				default:
					service.sendIfOpen(session, "ERROR", Map.of(
						"code", "UNSUPPORTED_TYPE",
						"message", "지원하지 않는 type: " + type));
			}

		} catch (JsonSyntaxException e) {
			service.sendIfOpen(session, "ERROR", Map.of(
				"code", "INVALID_JSON",
				"message", "JSON 파싱 실패"));
		} catch (Exception e) {
			e.printStackTrace();
			service.sendIfOpen(session, "ERROR", Map.of(
				"code", "SERVER_ERROR",
				"message", "요청 처리 실패: " + e.getMessage()));
		}
	}

	/**
	 * DB 상태가 변경되었을 때 호출하면, 로비 접속자 전체에게 방 목록을 갱신 전송한다.
	 *
	 * 호출 위치 예시:
	 * - 방 생성 성공 직후
	 * - 방 입장/퇴장 처리 직후 (인원수 변경)
	 * - 방 삭제 처리 직후
	 */
	public static void broadcastRoomList() {
		System.out.println("[LobbyWS] broadcastRoomList sessions=" + lobbySessions.size());
		for (Session s : lobbySessions) {
			try {
				service.sendRoomList(s); // DB에서 최신 조회 후 ROOM_LIST 전송
			} catch (Exception e) {
				System.err.println("[LobbyWS] broadcast failed wsSessionId=" + (s != null ? s.getId() : "null"));
				e.printStackTrace();
			}
		}
	}

	public void sendHostAutoEnter(Session s, String roomId, String roomName, int playType, String hostUserId) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("roomId", roomId);
		payload.put("roomName", roomName);
		payload.put("playType", playType);
		payload.put("hostUserId", hostUserId);

		sendRawIfOpen(s, "ROOM_AUTO_ENTER", payload);
	}

	/**
	 * LobbyWebSocketService를 거치지 않고도 간단 payload 전송이 필요할 때 사용.
	 * (CONNECTED 같은 초기 메시지 용)
	 */
	private void sendRawIfOpen(Session s, String type, Map<String, Object> payload) {
		if (s == null || !s.isOpen())
			return;

		Map<String, Object> message = new HashMap<>();
		message.put("type", type);
		message.put("payload", payload);

		try {
			s.getAsyncRemote().sendText(Parser.toJson(message));
		} catch (Exception e) {
			System.err.println("[LobbyWS] send failed");
			e.printStackTrace();
		}
	}
}
