package room.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lobby.ws.LobbyWebSocket;
import room.service.RoomService;

@WebServlet("/room/exit")
public class ExitRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final RoomService roomService = new RoomService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String ctx = request.getContextPath();
		String roomId = request.getParameter("roomId");
		HttpSession session = request.getSession(false);
		String userId = (session == null) ? null : (String)session.getAttribute("loginUserId");
		if (userId == null || userId.isBlank()) {
			response.sendRedirect(ctx + "/lobby?error=enter_failed");
			return;
		}

		try {
			roomService.exitAndHandleHost(roomId, userId);
			System.out.println("[EXIT] ctx=" + request.getContextPath() + " roomId=" + roomId + " userId=" + userId);
			LobbyWebSocket.broadcastRoomList();
			response.sendRedirect(ctx + "/lobby");
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(ctx + "/lobby?error=exit_failed");
		}
	}

}
