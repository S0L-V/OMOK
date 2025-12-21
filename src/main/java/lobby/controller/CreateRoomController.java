package lobby.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lobby.ws.LobbyWebSocket;
import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;

@WebServlet("/room/create")
public class CreateRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
			return;
		}

		String hostUserId = (String)session.getAttribute("loginUserId");

		if (hostUserId == null || hostUserId.isBlank()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
			return;
		}

		System.out.println("session = " + session);
		System.out.println("loginUserId = " + (session == null ? null : session.getAttribute("loginUserId")));
		System.out.println("loginNickname = " + (session == null ? null : session.getAttribute("loginNickname")));

		String roomName = request.getParameter("roomName");
		String roomPwd = request.getParameter("roomPwd");
		String isPublic = request.getParameter("isPublic");
		String playType = request.getParameter("playType");

		try {
			RoomDAO roomDAO = new RoomDAOImpl();

			roomDAO.createRoom(
				hostUserId,
				roomName,
				roomPwd,
				isPublic,
				playType);

			LobbyWebSocket.broadcastRoomList();

			response.sendRedirect(request.getContextPath() + "/lobby");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("방 생성 실패", e);
		}
	}
}
