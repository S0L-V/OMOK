package lobby.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import lobby.dto.ContextDTO;
import lobby.dto.Role;

public class LobbySessionContext {

	private static final LobbySessionContext INSTANCE = new LobbySessionContext();

	public static LobbySessionContext getInstance() {
		return INSTANCE;
	}

	private LobbySessionContext() {}

	private final Map<String, ContextDTO> contexts = new ConcurrentHashMap<>();

	public void connectSession(Session wsSession, HttpSession httpSession) {
		if (wsSession == null)
			return;

		String wsSessionId = wsSession.getId();

		ContextDTO context = new ContextDTO();
		context.setWsSessionId(wsSessionId);
		context.setHttpSession(httpSession);
		context.setRole(Role.GUEST);
		context.setLogin(false);

		if (httpSession != null) {
			Object userIdObj = httpSession.getAttribute("loginUserId");
			Object nickObj = httpSession.getAttribute("loginNickname");

			if (userIdObj != null) {
				context.setLogin(true);
				context.setUserId(String.valueOf(userIdObj));
				context.setNickname(nickObj != null ? String.valueOf(nickObj) : null);
				context.setRole(Role.USER);
			}
		}

		contexts.put(wsSessionId, context);
	}

	public void disconnectSession(Session wsSession) {
		if (wsSession == null)
			return;
		contexts.remove(wsSession.getId());
	}

	public boolean isLogin(Session session) {
		ContextDTO context = getContext(session);
		return context != null && Boolean.TRUE.equals(context.isLogin());
	}

	public String getUserId(Session session) {
		ContextDTO context = getContext(session);
		return context != null ? context.getUserId() : null;
	}

	public String getNickname(Session session) {
		ContextDTO context = getContext(session);
		return context != null ? context.getNickname() : null;
	}

	public Role getRole(Session session) {
		ContextDTO context = getContext(session);
		return context != null && context.getRole() != null ? context.getRole() : Role.GUEST;
	}

	public String getRoomId(Session session) {
		ContextDTO context = getContext(session);
		return context != null ? context.getRoomId() : null;
	}

	public void enterRoom(Session session, String roomId) {
		ContextDTO context = getContext(session);
		if (context != null)
			context.setRoomId(roomId);
	}

	public void leaveRoom(Session session) {
		ContextDTO context = getContext(session);
		if (context != null)
			context.setRoomId(null);
	}

	private ContextDTO getContext(Session session) {
		if (session == null)
			return null;
		return contexts.get(session.getId());
	}
}
