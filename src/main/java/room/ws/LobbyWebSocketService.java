package room.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;
import room.dto.RoomDTO;
import util.Parser;

public class LobbyWebSocketService {
	RoomDAO dao = new RoomDAOImpl();

	/**
	 * 현재 로비의 방 목록을 조회하여 클라이언트에게 전송한다.
	 *
	 *
	 * <p> - DB에서 방 목록을 조회한 뒤 WebSocket 메시지로 직렬화하여 전송한다.</p>
	 * <p> - WebSocket 세션이 유효하지 않으면 전송하지 않는다.</p>
	 *
	 *
	 * @param s 메시지를 전송할 WebSocket 세션
	 */
	public void sendRoomList(Session s) {
		try {
			List<RoomDTO> rooms = dao.getRoomList();
			Map<String, Object> payload = new HashMap<>();
			payload.put("rooms", rooms);
			sendIfOpen(s, "ROOM_LIST", payload);
		} catch (Exception e) {
			System.err.println("[WS] failed to send room list");
			e.printStackTrace();
		}
	}

	/**
	 * WebSocket 세션이 열려 있는 경우에만 메시지를 전송한다.
	 *
	 * <p>
	 * 메시지는 다음 JSON 구조로 전송된다.
	 * <pre>
	 * {
	 *   "type": "...",
	 *   "payload": { ... }
	 * }
	 * </pre>
	 * </p>
	 * @param s WebSocket 세션
	 * @param type 메시지 타입 (예:ROOM_ENTER, ERROR 등)
	 * @param payload 메시지 본문 데이터
	 */
	public void sendIfOpen(Session s, String type, Map<String, Object> payload) {
		if (s == null || !s.isOpen())
			return;

		Map<String, Object> message = new HashMap<>();
		message.put("type", type);
		message.put("payload", payload);

		try {
			s.getAsyncRemote().sendText(Parser.toJson(message));
		} catch (Exception e) {
			System.err.println("[WS] send failed");
			e.printStackTrace();
		}
	}
}
