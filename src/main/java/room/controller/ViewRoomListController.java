package room.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import room.dao.RoomDAO;
import room.dao.RoomDAOImpl;
import room.dto.RoomDTO;

@WebServlet("/lobby")
public class ViewRoomListController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final RoomDAO roomDAO = new RoomDAOImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		try {
			HttpSession session = request.getSession(false);
			List<RoomDTO> roomList = roomDAO.getRoomList();
			String loginUserId = session == null ? null : (String)session.getAttribute("loginUserId");
			String loginNickname = session == null ? null : (String)session.getAttribute("loginNickname");

			System.out.println(
				"[LOGIN] session=" + (session == null ? "null" : session.getId())
					+ " loginUserId=" + loginUserId
					+ " loginNickname=" + loginNickname);

			request.setAttribute("loginUserId", loginUserId);
			request.setAttribute("loginNickname", loginNickname);
			request.setAttribute("roomList", roomList);

			request.getRequestDispatcher("/WEB-INF/views/lobby.jsp").forward(request, response);
			return;
		} catch (Exception e) {
			throw new ServletException("방 목록 조회를 실패하였습니다.", e);
		}
	}
}
