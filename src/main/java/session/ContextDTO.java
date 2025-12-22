package session;

import javax.servlet.http.HttpSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContextDTO {
	String wsSessionId;
	HttpSession httpSession;

	boolean login = false;
	Role role = Role.GUEST;

	String userId;
	String nickname;
	String roomId;
}