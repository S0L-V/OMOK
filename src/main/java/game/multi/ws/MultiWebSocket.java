package game.multi.ws;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import config.WebSocketConfig;
import game.multi.dao.MultiPlayerDAO;
import game.multi.dao.MultiPlayerDAOImpl;
import game.multi.service.MultiGameService;
import game.multi.service.MultiGameService.SendJob;

@ServerEndpoint(value = "/game/multi/ws", configurator = WebSocketConfig.class)
public class MultiWebSocket {

	private static final MultiGameService service = new MultiGameService();

	private static final MultiPlayerDAO multiPlayerDao = new MultiPlayerDAOImpl();

	/* 
	 * room 단위 세션/유저/닉/슬롯 캐시
	 * - 이모지 브로드캐스트를 해당 room에만 하기 위함
	 * - GAME_MULTI_START 메시지를 dispatch에서 감지해 slot을 캐싱함
	 */
	private static final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>(); // sessionId -> roomId
	private static final Map<String, String> sessionUserMap = new ConcurrentHashMap<>(); // sessionId -> userId
	private static final Map<String, String> sessionNickMap = new ConcurrentHashMap<>(); // sessionId -> nickname
	private static final Map<String, Integer> sessionSlotMap = new ConcurrentHashMap<>(); // sessionId -> slot(0~3)

	private static final Map<String, Map<String, Session>> roomSessions = new ConcurrentHashMap<>(); // roomId -> (sessionId -> Session)

	private static final Gson gson = new Gson();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		try {
			// HttpSession에서 userId 가져오기
			HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
			String userId = null;
			if (httpSession != null) {
				userId = (String)httpSession.getAttribute("loginUserId");
			}

			// URL 쿼리 스트링에서 roomId 파싱
			String query = session.getRequestURI().getQuery();
			String roomId = getParameterValue(query, "roomId");

			if (roomId == null || roomId.trim().isEmpty()) {
				roomId = "default"; // 방 ID가 없을 때
			}

			// DB로 방 멤버 검증
			if (!multiPlayerDao.isMember(roomId, userId)) {
				session.close();
				return;
			}

			/* 
			 * room/session 캐시 등록
			 * SingleWebSocket과 동일 -> loginNickname 사용
			 */
			sessionRoomMap.put(session.getId(), roomId);
			if (userId != null)
				sessionUserMap.put(session.getId(), userId);

			String nickname = null;
			if (httpSession != null) {
				nickname = (String)httpSession.getAttribute("loginNickname");
			}
			if (nickname == null || nickname.isBlank()) {
				nickname = (userId != null ? userId : "unknown");
			}
			sessionNickMap.put(session.getId(), nickname);

			roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
				.put(session.getId(), session);

			// Service에 roomId와 userId 함께 전달
			List<SendJob> jobs = service.handleOpen(session, roomId, userId);
			dispatch(session, jobs);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 쿼리 스트링 파싱 헬퍼 메서드
	private String getParameterValue(String query, String name) {
		if (query == null)
			return null;
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			if (pair.length > 1 && pair[0].equals(name)) {
				return pair[1];
			}
		}
		return null;
	}

	@OnMessage
	public void onMessage(String msg, Session session) {
		try {
			/* 
			 * 이모지 채팅: "EMOJI_CHAT:smile" 텍스트 프로토콜
			 * - 같은 방에 브로드캐스트
			 * - SingleWebSocket과 동일한 JSON 포맷(type/payload) + slot 추가
			 */
			if (msg != null && msg.startsWith("EMOJI_CHAT:")) {
				String emojiKey = msg.substring("EMOJI_CHAT:".length()).trim();
				if (emojiKey.isEmpty())
					return;

				String roomId = sessionRoomMap.get(session.getId());
				if (roomId == null)
					return;

				String userId = sessionUserMap.get(session.getId());
				String nickname = sessionNickMap.get(session.getId());
				if (nickname == null || nickname.isBlank())
					nickname = (userId != null ? userId : "unknown");

				int slot = -1;
				Integer cachedSlot = sessionSlotMap.get(session.getId());
				if (cachedSlot != null)
					slot = cachedSlot;

				JsonObject root = new JsonObject();
				root.addProperty("type", "EMOJI_CHAT");

				JsonObject payload = new JsonObject();
				if (userId != null)
					payload.addProperty("from", userId);
				payload.addProperty("fromNick", nickname);
				payload.addProperty("emoji", emojiKey);
				if (slot >= 0)
					payload.addProperty("slot", slot);

				root.add("payload", payload);

				String json = gson.toJson(root);

				// room 내 세션에게만 전송
				Map<String, Session> targets = roomSessions.get(roomId);
				if (targets != null) {
					for (Session s : targets.values()) {
						if (s != null && s.isOpen()) {
							try {
								synchronized (s) {
									s.getBasicRemote().sendText(json);
								}
							} catch (Exception ignore) {}
						}
					}
				}
				return;
			}

			List<SendJob> jobs = service.handleMessage(session, msg);
			dispatch(session, jobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		try {
			/* room/session 캐시 정리 */
			String roomId = sessionRoomMap.remove(session.getId());
			sessionUserMap.remove(session.getId());
			sessionNickMap.remove(session.getId());
			sessionSlotMap.remove(session.getId());

			if (roomId != null) {
				Map<String, Session> targets = roomSessions.get(roomId);
				if (targets != null) {
					targets.remove(session.getId());
					if (targets.isEmpty()) {
						roomSessions.remove(roomId);
					}
				}
			}

			List<SendJob> jobs = service.handleClose(session);
			dispatch(session, jobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("소켓 에러 발생: " + t.getMessage());
	}

	private void dispatch(Session fallback, List<SendJob> jobs) {
		// 전체에게 보내야 할 경우를 대비해 현재 열려있는 모든 세션을 가져옴
		// Service가 target=null을 주면 모두에게 보냄

		for (SendJob job : jobs) {
			try {
				/*
				 * GAME_MULTI_START를 감지해서 slot 캐싱
				 * GameRoom이 보내는 {"type":"GAME_MULTI_START","slot":i,...}를 여기서 읽어둠
				 */
				if (job.target() != null) {
					cacheSlotIfStart(job.target(), job.text());
				}
				// ==================================================

				// 1. 전체 전송 (Broadcast)
				if (job.target() == null) {
					// 타겟이 없으면(null) => 브로드캐스트 (전체 전송)
					for (Session s : fallback.getOpenSessions()) {
						if (s.isOpen()) {
							try {
								// 충돌 방지용 동기화 처리
								synchronized (s) {
									s.getBasicRemote().sendText(job.text());
								}
							} catch (Exception e) {}
						}
					}
				}
				// 2. 개별 전송 (Unicast)
				else {
					if (job.target().isOpen()) {
						try {
							synchronized (job.target()) {
								job.target().getBasicRemote().sendText(job.text());
							}
						} catch (Exception e) {}
					}
				}
			} catch (Exception ignore) {}
		}
	}

	/* GAME_MULTI_START 메시지에서 slot을 캐싱 */
	private void cacheSlotIfStart(Session target, String text) {
		try {
			if (text == null)
				return;
			// 파싱 최소화
			if (!text.contains("\"type\"") || !text.contains("GAME_MULTI_START"))
				return;

			JsonObject obj = gson.fromJson(text, JsonObject.class);
			if (obj == null || !obj.has("type"))
				return;

			String type = obj.get("type").getAsString();
			if (!"GAME_MULTI_START".equals(type))
				return;

			if (obj.has("slot")) {
				int slot = obj.get("slot").getAsInt();
				sessionSlotMap.put(target.getId(), slot);
			}
		} catch (Exception ignore) {}
	}
}
