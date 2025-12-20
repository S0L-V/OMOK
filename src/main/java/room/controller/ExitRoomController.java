package room.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import room.dao.RoomPlayerDAO;
import room.dao.RoomPlayerDAOImpl;

@WebServlet("/room/exit")
public class ExitRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final RoomPlayerDAO roomPlayerDao = new RoomPlayerDAOImpl();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		String ctx = request.getContextPath();

		try {
			String roomId = request.getParameter("roomId");
			if (roomId == null || roomId.isBlank()) {
				response.sendRedirect(ctx + "/lobby?error=missing_room_id");
				return;
			}

			HttpSession session = request.getSession(false);
			String userId = (session == null) ? null : (String)session.getAttribute("loginUserId");
			if (userId == null || userId.isBlank()) {
				response.sendRedirect(ctx + "/login?error=unauthorized");
				return;
			}

			roomPlayerDao.exit(roomId, userId);

			response.sendRedirect(ctx + "/lobby?leftRoomId=" +
				URLEncoder.encode(roomId, StandardCharsets.UTF_8));

		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(ctx + "/lobby?error=exit_failed");
		}
	}

}
