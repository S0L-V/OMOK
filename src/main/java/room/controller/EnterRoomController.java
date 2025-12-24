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

import lobby.ws.LobbyWebSocket;
import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;
import room.dao.RoomPlayerDAO;
import room.dao.RoomPlayerDAOImpl;

@WebServlet("/room/enter")
public class EnterRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final RoomPlayerDAO roomPlayerDao = new RoomPlayerDAOImpl();
	private final RoomDAO roomDAO = new RoomDAOImpl();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		String ctx = request.getContextPath();

		try {
			String roomId = request.getParameter("roomId");
			String playType = request.getParameter("playType");
			String roomPwd = request.getParameter("roomPwd");
			String isPublic = request.getParameter("isPublic");

			System.out.println("isPublic: " + isPublic);

			if (roomId == null || roomId.isBlank() || playType == null || playType.isBlank()) {
				response.sendRedirect(ctx + "/lobby?error=missing_room_info");
				return;
			}

			HttpSession session = request.getSession(false);
			String userId = (session == null) ? null : (String)session.getAttribute("loginUserId");
			if (userId == null || userId.isBlank()) {
				response.sendRedirect(ctx + "/lobby?error=enter_failed");
				return;
			}

			if ("private".equals(isPublic)) {
				if (roomPwd == null || roomPwd.isBlank()) {
					response.sendRedirect(ctx + "/lobby?error=need_password");
					return;
				}

				if (!roomDAO.matchRoomPassword(roomId, roomPwd)) {
					response.sendRedirect(ctx + "/lobby?error=wrong_password");
					return;
				}
			}

			roomPlayerDao.enterIfAbsent(roomId, userId);

			System.out.println("[ENTER] ctx=" + request.getContextPath() + " roomId=" + roomId + " userId=" + userId);
			LobbyWebSocket.broadcastRoomList();
			response
				.sendRedirect(ctx + "/room?roomId=" + URLEncoder.encode(roomId, StandardCharsets.UTF_8) + "&playType="
					+ URLEncoder.encode(playType, StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(ctx + "/lobby?error=enter_failed");
		}
	}

}
