package room.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import com.google.gson.Gson;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;
import room.dao.RoomPlayerDAO;
import room.dao.RoomPlayerDAOImpl;
import room.dto.RoomPlayerDTO;
import session.SessionContext;

/**
 * Room WS 서비스
 * - sendIfOpen: {type, payload} 포맷 전송
 * - onEnter/onChat/onExit: room registry & broadcast
 */
public class RoomWebSocketService {

	private static final Gson gson = new Gson();
	private static final SessionContext sessionContext = SessionContext.getInstance();
	private static final RoomSessionRegistry roomRegistry = RoomSessionRegistry.getInstance();
	private final RoomPlayerDAO roomPlayerDao = new RoomPlayerDAOImpl();
	private final RoomDAO roomDao = new RoomDAOImpl();

	public void sendIfOpen(Session s, String type, Map<String, Object> payload) {
		if (s == null || !s.isOpen())
			return;

		Map<String, Object> message = new HashMap<>();
		message.put("type", type);
		message.put("payload", payload);

		try {
			s.getAsyncRemote().sendText(gson.toJson(message));
		} catch (Exception ignored) {}
	}

	public void broadcastRoomPlayerList(String roomId) throws Exception {
		List<RoomPlayerDTO> players = roomPlayerDao.getPlayerList(roomId);
		broadcast(roomId, "ROOM_PLAYER_LIST", Map.of("roomPlayerList", players));
	}

	public void onEnter(Session session, String roomId) {
		roomRegistry.removeFromAnyRoom(session);

		roomRegistry.join(roomId, session);

		broadcast(roomId, "USER_ENTER", Map.of(
			"userId", sessionContext.getUserId(session),
			"nickname", sessionContext.getNickname(session)));

		try {
			broadcastRoomPlayerList(roomId);
		} catch (Exception e) {
			System.out.println("[RoomWS] Broadcast Player List FAIL");
			e.printStackTrace();
		}

	}

	public void onChat(Session session, String roomId, String text) {
		String from = sessionContext.getNickname(session);
		if (from == null || from.isBlank())
			from = sessionContext.getUserId(session);
		if (from == null || from.isBlank())
			from = "unknown";

		broadcast(roomId, "ROOM_CHAT", Map.of(
			"from", from,
			"text", text));
	}

	public void onExit(Session session, String roomId, String result) {
		if (roomId == null || roomId.isBlank())
			return;

		roomRegistry.leave(roomId, session);

		if ("HOST_CHANGE".equals(result)) {
			String hostUserId;
			try {
				hostUserId = roomDao.getHostUserId(roomId);
				broadcastHostChanged(roomId, hostUserId);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		broadcast(roomId, "USER_EXIT", Map.of(
			"userId", sessionContext.getUserId(session),
			"nickname", sessionContext.getNickname(session)));

	}

	/** @OnClose/@OnError 최종 정리 */
	public void cleanup(Session session) {
		String roomId = sessionContext.getRoomId(session);
		if (roomId != null && !roomId.isBlank()) {
			onExit(session, roomId, null);
			sessionContext.leaveRoom(session);
		} else {
			roomRegistry.removeFromAnyRoom(session);
		}
	}

	/**
	 * 게임 시작 브로드캐스트
	 * - 클라이언트는 GAME_START 수신 시 location.href로 이동
	 */
	public void broadcastGameStart(String roomId, String gameId, String playType) {
		if (roomId == null || roomId.isBlank())
			return;

		Map<String, Object> payload = new HashMap<>();
		payload.put("roomId", roomId);
		payload.put("playType", playType); // "0"(개인전) / "1"(팀전)

		broadcast(roomId, "GAME_START", payload);
	}

	public void broadcastHostChanged(String roomId, String hostUserId) {
		if (roomId == null || roomId.isBlank())
			return;

		Map<String, Object> payload = new HashMap<>();
		payload.put("roomId", roomId);
		payload.put("hostUserId", hostUserId);

		broadcast(roomId, "HOST_CHANGE", payload);
	}

	private void broadcast(String roomId, String type, Map<String, Object> payload) {
		Set<Session> sessions = roomRegistry.getSessions(roomId);
		for (Session s : sessions) {
			sendIfOpen(s, type, payload);
		}
	}
}
