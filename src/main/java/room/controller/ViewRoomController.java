package room.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/room")
public class ViewRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		try {
			String roomId = request.getParameter("roomId");

			if (roomId == null) {
				response.sendRedirect("/lobby");
				return;
			}

			request.setAttribute("roomId", roomId);

			request.getRequestDispatcher("/WEB-INF/views/room.jsp").forward(request, response);
			return;
		} catch (Exception e) {
			throw new ServletException("방 입장을 실패하였습니다.", e);
		}
	}
}
