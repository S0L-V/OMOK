package room.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.single.dao.SinglePlayerDAO;
import game.single.dao.SinglePlayerDAOImpl;

/**
 * Servlet implementation class GameToRoomController
 */
@WebServlet("/room/playersToRoom")
public class GameToRoomController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final SinglePlayerDAO singlePlayerDAO = new SinglePlayerDAOImpl();
//	private final MultiPlayerDAO multiPlayerDAO = new MultiPlayerDAOImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String roomId = req.getParameter("roomId");
        if (roomId == null || roomId.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"ok\":false,\"message\":\"roomId is required\"}");
            return;
        }

        try {
            int updated = singlePlayerDAO.updatePlayersToRoom(roomId); // IN_GAME -> IN_ROOM
            resp.getWriter().write("{\"ok\":true,\"updated\":" + updated + "}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"ok\":false,\"message\":\"server error\"}");
        }
    }

}
