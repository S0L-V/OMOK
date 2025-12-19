package ws.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import login.dto.LoginKakaoUserInfoDTO;

public class SessionContext {

	private static final SessionContext ctx = new SessionContext();

	public static SessionContext getInstance() {
		return ctx;
	}

	private SessionContext() {}

	private final Map<String, ContextDTO> contexts = new ConcurrentHashMap<>();

	public void connectSession(Session wsSession, HttpSession httpSession) {
		ContextDTO ctx = new ContextDTO();
		ctx.wsSessionId = wsSession.getId();
		ctx.httpSession = httpSession;

		if (httpSession != null) {
			Object loginUser = httpSession.getAttribute("loginUser");
			if (loginUser instanceof LoginKakaoUserInfoDTO user) {
				ctx.login = true;
				ctx.userId = user.getUserId();
				ctx.nickname = user.getNickname();
				ctx.role = Role.USER;
			} else {
				ctx.role = Role.GUEST;
			}
		} else {
			ctx.role = Role.GUEST;
		}

		contexts.put(wsSession.getId(), ctx);
	}

	public void disconnectSession(Session wsSession) {
		if (wsSession == null)
			return;
		contexts.remove(wsSession.getId());
	}

	public boolean isLogin(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		return ctx != null && ctx.login;
	}

	public String getUserId(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		return ctx != null ? ctx.userId : null;
	}

	public String getNickname(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		return ctx != null ? ctx.nickname : null;
	}

	public Role getRole(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		return ctx != null ? ctx.role : Role.GUEST;
	}

	public String getRoomId(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		return ctx != null ? ctx.roomId : null;
	}

	public void enterRoom(Session session, String roomId) {
		ContextDTO ctx = contexts.get(session.getId());
		if (ctx != null) {
			ctx.roomId = roomId;
		}
	}

	public void leaveRoom(Session session) {
		ContextDTO ctx = contexts.get(session.getId());
		if (ctx != null) {
			ctx.roomId = null;
		}
	}

}
