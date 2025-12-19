package ws.session;

import javax.servlet.http.HttpSession;

public class ContextDTO {
	String wsSessionId;
	HttpSession httpSession;

	boolean login = false;
	Role role = Role.GUEST;

	String userId;
	String nickname;
	String roomId;
}