package login.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/lobby")
public class LobbyControllerTest extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		// ✅ 세션이 있으면 가져오고, 없으면 새로 만들지 않게 false
		HttpSession session = request.getSession(false);

		String loginUserId = null;
		String loginNickname = null;

		if (session != null) {
			loginUserId = (String)session.getAttribute("loginUserId");
			loginNickname = (String)session.getAttribute("loginNickname");
		}

		// ✅ JSP에서 출력할 수 있게 request에 담아줌
		request.setAttribute("loginUserId", loginUserId);
		request.setAttribute("loginNickname", loginNickname);

		// ✅ WEB-INF 아래 JSP로 forward (직접 URL 접근 불가, 서블릿 통해서만 접근)
		request.getRequestDispatcher("/login/lobbyTest.jsp").forward(request, response);
	}
}
