package game.single.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SingleGameServlet
 */
@WebServlet("/single")
public class SingleGameController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		//		request.setAttribute("wsUrl", ".../omok");             wsUrl 넘기기
		//		color 넘기기
		//		request.setAttribute("roomId", roomId);
		request.getRequestDispatcher("/WEB-INF/views/game/single.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		doGet(request, response);
	}

}