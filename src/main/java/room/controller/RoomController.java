package room.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import room.dao.RoomPlayerDAO;
import room.dao.RoomPlayerDAOImpl;
import room.dto.RoomActiveCountDTO;

@WebServlet("/room/*")
public class RoomController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final Gson gson = new Gson();
	private final RoomPlayerDAO roomPlayerDAO = new RoomPlayerDAOImpl();

	@FunctionalInterface
	private interface Handler {
		void handle(HttpServletRequest req, HttpServletResponse res, String userId) throws Exception;
	}

	private final Map<String, Handler> getHandlers = new HashMap<>();

	@Override
	public void init() {
		getHandlers.put("count", this::handleGetCount);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		String action = getAction(req);
		if (action == null) {
			sendJson(res, 400, ApiError.of("잘못된 요청입니다."));
			return;
		}

		String userId = getLoginUserId(req);
		if (userId == null || userId.isBlank()) {
			sendJson(res, 401, ApiError.of("로그인이 필요합니다."));
			return;
		}

		Handler handler = getHandlers.get(action);
		if (handler == null) {
			sendJson(res, 404, ApiError.of("존재하지 않는 API입니다."));
			return;
		}

		try {
			handler.handle(req, res, userId);
		} catch (Exception e) {
			e.printStackTrace();
			sendJson(res, 500, ApiError.of("서버 오류가 발생했습니다."));
		}
	}

	/**
	 * GET /room/count?roomId=
	 * 응답: { ok:true, data:{ roomId, activeCount } }
	 */
	private void handleGetCount(HttpServletRequest req, HttpServletResponse res, String userId) throws Exception {
		String roomId = req.getParameter("roomId");
		if (roomId == null || roomId.isBlank()) {
			sendJson(res, 400, ApiError.of("roomId가 필요합니다."));
			return;
		}
		int activeCount = roomPlayerDAO.countActivePlayers(roomId);
		RoomActiveCountDTO dto = new RoomActiveCountDTO(roomId, activeCount);
		sendJson(res, 200, ApiSuccess.of(dto));
	}

	private String getAction(HttpServletRequest req) {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || "/".equals(pathInfo))
			return null;
		return pathInfo.substring(1);
	}

	private String getLoginUserId(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		return (session == null) ? null : (String)session.getAttribute("loginUserId");
	}

	private void sendJson(HttpServletResponse res, int status, Object body) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json; charset=UTF-8");
		res.getWriter().write(gson.toJson(body));
	}

	private static class ApiSuccess<T> {
		private final boolean ok = true;
		private final T data;

		private ApiSuccess(T data) {
			this.data = data;
		}

		public static <T> ApiSuccess<T> of(T data) {
			return new ApiSuccess<>(data);
		}

		public boolean isOk() {
			return ok;
		}

		public T getData() {
			return data;
		}
	}

	private static class ApiError {
		private final boolean ok = false;
		private final String message;

		private ApiError(String message) {
			this.message = message;
		}

		public static ApiError of(String message) {
			return new ApiError(message);
		}

		public boolean isOk() {
			return ok;
		}

		public String getMessage() {
			return message;
		}
	}
}
