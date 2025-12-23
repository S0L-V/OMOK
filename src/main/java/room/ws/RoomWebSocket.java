package room.ws;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonSyntaxException;

import config.WebSocketConfig;
import lobby.ws.LobbyWebSocket;
import room.service.RoomService;
import session.RoomTransitionRegistry;
import session.SessionContext;
import util.Parser;

/**
 * 방 WebSocket 엔드포인트
 *
 * 메시지 구조:
 * { "type": "...", "payload": { ... } }
 *
 * 클라이언트 요청(type):
 * - ROOM_ENTER : 방 입장(등록) (payload.roomId 필요, 없으면 query roomId 사용)
 * - ROOM_CHAT  : 채팅 전송 (payload.text 필요)
 * - ROOM_EXIT  : 방 나가기(옵션, 최종 정리 @OnClose)
 *
 * 연결 URL:
 * ws://host/ws/room?roomId=xxx
 */
@ServerEndpoint(value = "/ws/room", configurator = WebSocketConfig.class)
public class RoomWebSocket {

	private static final SessionContext sessionContext = SessionContext.getInstance();
	private final RoomWebSocketService service = new RoomWebSocketService();
	private final RoomService roomService = new RoomService();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
		sessionContext.connectSession(session, httpSession);

		String roomIdFromQuery = getQueryParam(session.getQueryString(), "roomId");
		if (roomIdFromQuery != null && !roomIdFromQuery.isBlank()) {
			sessionContext.enterRoom(session, roomIdFromQuery);
		}

		System.out.println("[RoomWS] CONNECTED wsSessionId=" + session.getId()
			+ " userId=" + sessionContext.getUserId(session)
			+ " nick=" + sessionContext.getNickname(session)
			+ " role=" + sessionContext.getRole(session)
			+ " roomId(query)=" + roomIdFromQuery);

		Map<String, Object> payload = new HashMap<>();
		payload.put("login", sessionContext.isLogin(session));
		payload.put("userId", sessionContext.getUserId(session));
		payload.put("nickname", sessionContext.getNickname(session));
		payload.put("role", sessionContext.getRole(session).name());
		payload.put("roomId", roomIdFromQuery);

		service.sendIfOpen(session, "CONNECTED", payload);
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
				case "ROOM_ENTER": {
					if (!sessionContext.isLogin(session)) {
						service.sendIfOpen(session, "ERROR", Map.of(
							"code", "UNAUTHORIZED",
							"message", "로그인 후 방에 입장할 수 있습니다."));
						safeClose(session, CloseCodes.VIOLATED_POLICY, "unauthorized");
						return;
					}

					String roomId = Parser.asString(payload.get("roomId"));
					String roomIdFromQuery = sessionContext.getRoomId(session);

					if ((roomId == null || roomId.isBlank()) && roomIdFromQuery != null) {
						roomId = roomIdFromQuery;
					}

					if (roomId == null || roomId.isBlank()) {
						service.sendIfOpen(session, "ERROR", Map.of(
							"code", "MISSING_ROOM_ID",
							"message", "roomId가 없습니다."));
						safeClose(session, CloseCodes.CANNOT_ACCEPT, "roomId required");
						return;
					}

					/* query와 payload가 동시에 있는 경우 불일치 방지 */
					if (roomIdFromQuery != null && !roomIdFromQuery.isBlank() && !roomIdFromQuery.equals(roomId)) {
						service.sendIfOpen(session, "ERROR", Map.of(
							"code", "ROOM_ID_MISMATCH",
							"message", "roomId가 일치하지 않습니다."));
						safeClose(session, CloseCodes.CANNOT_ACCEPT, "roomId mismatch");
						return;
					}

					sessionContext.enterRoom(session, roomId);

					service.onEnter(session, roomId);

					service.sendIfOpen(session, "ROOM_ENTER", Map.of("roomId", roomId));
					break;
				}

				case "ROOM_CHAT": {
					String roomId = sessionContext.getRoomId(session);
					if (roomId == null || roomId.isBlank()) {
						service.sendIfOpen(session, "ERROR", Map.of(
							"code", "NOT_IN_ROOM",
							"message", "방에 입장하지 않았습니다."));
						return;
					}

					String chatText = Parser.asString(payload.get("text"));
					if (chatText == null || chatText.isBlank())
						return;

					service.onChat(session, roomId, chatText);
					break;
				}

				case "ROOM_EXIT": {
					String roomId = sessionContext.getRoomId(session);

					service.onExit(session, roomId, "ROOM_EXIT");
					sessionContext.leaveRoom(session);

					service.sendIfOpen(session, "ROOM_EXIT", Map.of());
					safeClose(session, CloseCodes.NORMAL_CLOSURE, "ROOM_EXIT");
					break;
				}

				case "GAME_START": {
					service.sendIfOpen(session, "GAME_START", Map.of());
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

	private final RoomTransitionRegistry transitionRegistry = RoomTransitionRegistry.getInstance();

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("[RoomWS] CLOSE wsSessionId=" + session.getId()
			+ " code=" + reason.getCloseCode()
			+ " reason=" + reason.getReasonPhrase());

		String roomId = sessionContext.getRoomId(session);
		String userId = sessionContext.getUserId(session);

		boolean hasCtx = roomId != null && !roomId.isBlank() && userId != null && !userId.isBlank();

		boolean isNavigateClose = reason != null
			&& reason.getCloseCode() != null
			&& reason.getCloseCode().getCode() == 1000
			&& "NAVIGATE_TO_GAME".equals(reason.getReasonPhrase());

		boolean isTransition = hasCtx && transitionRegistry.isMoving(roomId, userId);

		try {
			if (hasCtx && (isNavigateClose || isTransition)) {
				System.out.println("[RoomWS] skip exit (transition) roomId=" + roomId + " userId=" + userId
					+ " by=" + (isNavigateClose ? "CloseReason" : "Registry"));

				service.cleanup(session);
				sessionContext.disconnectSession(session);
				if (isTransition)
					transitionRegistry.clear(roomId, userId);
				return;
			}

			if (hasCtx) {
				String result = roomService.exitAndHandleHost(roomId, userId);
				System.out.println("[RoomWS][EXIT] roomId=" + roomId + " userId=" + userId + " result=" + result);

				service.onExit(session, roomId, result);
				LobbyWebSocket.broadcastRoomList();
			}
		} catch (Exception e) {
			System.err.println("[RoomWS] onClose failed roomId=" + roomId + " userId=" + userId);
			e.printStackTrace();
		} finally {
			service.cleanup(session);
			sessionContext.disconnectSession(session);
		}
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		System.err.println("[RoomWS] ERROR wsSessionId=" + (session != null ? session.getId() : "null"));
		thr.printStackTrace();

		if (session != null) {
			service.cleanup(session);
			sessionContext.disconnectSession(session);
		}
	}

	private void safeClose(Session session, CloseCodes code, String reason) {
		try {
			if (session != null && session.isOpen()) {
				session.close(new CloseReason(code, reason));
			}
		} catch (Exception ignored) {}
	}

	private String getQueryParam(String queryString, String key) {
		if (queryString == null || queryString.isBlank())
			return null;

		for (String pair : queryString.split("&")) {
			String[] kv = pair.split("=", 2);
			if (kv.length == 2 && key.equals(kv[0])) {
				return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
			}
		}
		return null;
	}
}
