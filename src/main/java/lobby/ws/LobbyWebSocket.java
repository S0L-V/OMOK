package lobby.ws;

import java.util.HashMap;
import java.util.Map;

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
 * <pre>
 * { "type": "...", "payload": { ... } }
 * </pre>
 * </p>
 *
 * <p>
 * 지원하는 클라이언트 요청(type):
 * <ul>
 *   <li>{@code LOBBY_ENTER} : 로비 진입(방 목록 전송)</li>
 *   <li>{@code ROOM_LIST} : 방 목록 재요청</li>
 *   <li>{@code ROOM_ENTER} : 방 입장(roomId 필요)</li>
 *   <li>{@code ROOM_EXIT} : 방 퇴장</li>
 * </ul>
 * </p>
 *
 * <p>
 * 연결 시 HttpSession(로그인 세션)을 {@link WebSocketConfig}를 통해 주입받아
 * WebSocket 세션 컨텍스트에 연결한다.
 * </p>
 */

@ServerEndpoint(value = "/ws/lobby", configurator = WebSocketConfig.class)
public class LobbyWebSocket {

	private static final SessionContext sessionContext = SessionContext.getInstance();
	private final LobbyWebSocketService service = new LobbyWebSocketService();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());

		sessionContext.connectSession(session, httpSession);

		System.out.println(
			"[LobbyWS] CONNECTED wsSessionId=" + session.getId()
				+ " userId=" + sessionContext.getUserId(session)
				+ " nick=" + sessionContext.getNickname(session)
				+ " role=" + sessionContext.getRole(session));

		Map<String, Object> payload = new HashMap<>();
		payload.put("login", sessionContext.isLogin(session));
		payload.put("userId", sessionContext.getUserId(session));
		payload.put("nickname", sessionContext.getNickname(session));
		payload.put("role", sessionContext.getRole(session).name());

		Map<String, Object> message = new HashMap<>();
		message.put("type", "CONNECTED");
		message.put("payload", payload);

		session.getAsyncRemote().sendText(Parser.toJson(message));
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("[LobbyWS] CLOSE wsSessionId=" + session.getId()
			+ " code=" + reason.getCloseCode()
			+ " reason=" + reason.getReasonPhrase());

		sessionContext.disconnectSession(session);
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		System.err.println("[LobbyWS] ERROR wsSessionId=" + (session != null ? session.getId() : "null"));
		thr.printStackTrace();

		if (session != null) {
			sessionContext.disconnectSession(session);
		}
	}

	/**
	 * 클라이언트로부터 수신한 메시지를 type 기준으로 분기 처리한다.
	 *
	 * <p>
	 * 유효하지 않은 메시지에 대해서는 {@code ERROR} 타입으로 응답한다.
	 * </p>
	 *
	 * @param session WebSocket 세션
	 * @param text JSON 문자열 메시지
	 */
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
}
