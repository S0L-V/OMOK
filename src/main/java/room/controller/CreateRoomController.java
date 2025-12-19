package room.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;

@WebServlet("/room/create")
public class CreateRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		String hostUserId = request.getParameter("hostUserId");
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

			response.sendRedirect(request.getContextPath() + "/lobby");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("방 생성 실패", e);
		}
	}
}
