package game.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;
import room.dao.RoomPlayerDAO;
import room.dao.RoomPlayerDAOImpl;
import room.ws.RoomWebSocketService;

@WebServlet("/game/start")
public class GameStartController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final RoomDAO roomDao = new RoomDAOImpl();
	private final RoomPlayerDAO roomPlayerDao = new RoomPlayerDAOImpl();
	private final RoomWebSocketService service = new RoomWebSocketService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		String loginUserId = (session == null) ? null : (String)session.getAttribute("loginUserId");

		if (loginUserId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
			return;
		}

		String roomId = request.getParameter("roomId");
		String playType = request.getParameter("playType");

		if (roomId == null || roomId.isBlank() || playType == null || playType.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "roomId와 playType이 필요합니다.");
			return;
		}

		try {
			String hostUserId = roomDao.getHostUserId(roomId);

			if (hostUserId == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 방입니다.");
				return;
			}

			System.out.println(hostUserId + " " + loginUserId);

			if (!loginUserId.equals(hostUserId)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "게임 시작 권한이 없습니다. (host만 가능)");
				return;
			}
			int updated = roomPlayerDao.updatePlayersToInGame(roomId);

			System.out.println("[GameStart] roomId=" + roomId + " updatedPlayers=" + updated);

			service.broadcastGameStart(roomId, hostUserId, playType);

			String playTypeId = playType.equals("0") ? "single" : "multi";
			response.sendRedirect(
				request.getContextPath() + "/game/" + playTypeId + "?roomId="
					+ URLEncoder.encode(roomId, StandardCharsets.UTF_8));

		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게임 시작 처리 중 오류가 발생했습니다.");
		}
	}
}
