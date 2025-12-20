package record.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import login.vo.UserInfoVo;
import record.dto.GameResultDTO;
import record.dto.GameResultSaveRequest;
import record.service.GameResultService;
import record.service.GameResultServiceImpl;

@WebServlet("/record/*")
public class GameResultController extends HttpServlet {

	private GameResultService gameResultService;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		try {
			this.gameResultService = new GameResultServiceImpl();
			this.gson = new Gson();
			System.out.println("[Controller] GameResultController 초기화 완료");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("GameResultController 초기화 실패");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			sendError(res, 400, "잘못된 요청입니다.");
			return;
		}

		String[] paths = pathInfo.split("/");

		if (paths.length < 3) {
			sendError(res, 400, "잘못된 요청입니다.");
			return;
		}

		String action = paths[1]; // history / stats
		String userId = paths[2]; // userId

		switch (action) {
			case "history":
				handleGetGameHistory(res, userId);
				break;
			case "stats":
				handleGetUserStats(res, userId);
				break;
			default:
				sendError(res, 404, "존재하지 않는 API입니다.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if ("/save".equals(pathInfo)) {
			handleSaveGameResult(req, res);
		} else {
			sendError(res, 404, "존재하지 않는 API입니다.");
		}
	}

	/**
	 * POST /record/save - 게임 결과 저장
	 */
	private void handleSaveGameResult(HttpServletRequest req, HttpServletResponse res) throws IOException {
		System.out.println("[Controller] 게임 결과 저장 요청 받음");

		GameResultSaveRequest body = gson.fromJson(req.getReader(), GameResultSaveRequest.class);

		if (body == null || body.getGameId() == null || body.getResults() == null) {
			sendError(res, 400, "잘못된 데이터입니다.");
			return;
		}

		System.out.println("gameId: " + body.getGameId());
		System.out.println("roomId: " + body.getRoomId());
		System.out.println("플레이어 수: " + body.getResults().size());

		boolean success = gameResultService.saveGameResult(body);

		if (success) {
			System.out.println("[Controller] 게임 결과 저장 완료");
			sendSuccess(res, "게임 결과가 저장되었습니다.");
		} else {
			System.err.println("[Controller] 게임 결과 저장 실패");
			sendError(res, 500, "게임 결과 저장에 실패했습니다.");
		}
	}

	/**
	 * GET /record/history/{userId} - 게임 이력 조회
	 */
	private void handleGetGameHistory(HttpServletResponse res, String userId) throws IOException {
		List<GameResultDTO> history = gameResultService.getUserGameHistory(userId, 20);
		sendSuccess(res, history);
	}

	/**
	 * GET /record/stats/{userId} - 사용자 통계 조회
	 */
	private void handleGetUserStats(HttpServletResponse res, String userId) throws IOException {
		UserInfoVo stats = gameResultService.getUserStats(userId);

		if (stats != null) {
			sendSuccess(res, stats);
		} else {
			sendError(res, 404, "사용자 정보를 찾을 수 없습니다.");
		}
	}

	/**
	 * 성공 응답 (Gson 사용)
	 */
	private void sendSuccess(HttpServletResponse res, Object data) throws IOException {
		res.setStatus(200);
		res.setContentType("application/json; charset=UTF-8");

		ApiResponse response = new ApiResponse(true, data, null);
		res.getWriter().write(gson.toJson(response));
	}

	/**
	 * 에러 응답 (Gson 사용)
	 */
	private void sendError(HttpServletResponse res, int statusCode, String message) throws IOException {
		res.setStatus(statusCode);
		res.setContentType("application/json; charset=UTF-8");

		ApiResponse response = new ApiResponse(false, null, message);
		res.getWriter().write(gson.toJson(response));
	}

	/**
	 * API 응답 포맷
	 */
	private static class ApiResponse {
		boolean success;
		Object data;
		String message;

		ApiResponse(boolean success, Object data, String message) {
			this.success = success;
			this.data = data;
			this.message = message;
		}
	}
}
