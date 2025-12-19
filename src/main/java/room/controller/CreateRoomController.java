package room.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;

@WebServlet("/room/create")
public class CreateRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		// ✅ 1) 세션에서 로그인 유저 ID 가져오기 (이게 FK의 부모키 users.id)
		HttpSession session = request.getSession(false);

		System.out.println("session = " + session);
		System.out.println("loginUserId = " + (session == null ? null : session.getAttribute("loginUserId")));
		System.out.println("loginNickname = " + (session == null ? null : session.getAttribute("loginNickname")));

		// ✅ 로그인 안 된 상태면 방 생성 막기
		if (session == null || session.getAttribute("loginUserId") == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp?error=login_required");
			return;
		}

		// ✅ 여기 중요: hostUserId는 request parameter가 아니라 세션에서!
		String hostUserId = (String)session.getAttribute("loginUserId");

		// 2) 나머지 값들은 폼 파라미터에서 받는 게 맞음
		String roomName = request.getParameter("roomName");
		String roomPwd = request.getParameter("roomPwd");
		String isPublic = request.getParameter("isPublic");
		String playType = request.getParameter("playType");

		try {
			RoomDAO roomDAO = new RoomDAOImpl();

			roomDAO.createRoom(
				hostUserId, // ✅ users.id와 동일한 값이 들어가야 FK 통과
				roomName,
				roomPwd,
				isPublic,
				playType);

			response.sendRedirect(request.getContextPath() + "/lobby");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("방 생성 실패", e);
		}
	}

}
