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

@WebServlet("/room")
public class ViewRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final RoomDAO roomDAO = new RoomDAOImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		try {
			String roomId = request.getParameter("roomId");
			String playType = request.getParameter("playType");
			HttpSession session = request.getSession(false);

			if (playType == null || roomId == null || session == null) {
				response.sendRedirect("/lobby");
				return;
			}

			request.setAttribute("roomId", roomId);
			request.setAttribute("playType", playType);

			String userId = (String)session.getAttribute("loginUserId");
			String hostUserId = roomDAO.findHostUserIdByRoomId(roomId);

			if (hostUserId == null) {
				response.sendRedirect("/lobby?error=host_not_found");
				return;
			}

			session.setAttribute("hostUserId", hostUserId);
			session.setAttribute("userId", userId);

			request.getRequestDispatcher("/WEB-INF/views/room.jsp").forward(request, response);
			return;
		} catch (Exception e) {
			throw new ServletException("방 입장을 실패하였습니다.", e);
		}
	}
}
