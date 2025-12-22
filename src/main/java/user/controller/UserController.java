package user.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import login.vo.UserInfoVo;
import util.DB;

/**
 * 사용자 관리 Controller
 * - 닉네임 검색
 * - 랭킹 조회
 */
@WebServlet("/user/*")
public class UserController extends HttpServlet {

	private Gson gson;

	@Override
	public void init() throws ServletException {
		this.gson = new Gson();
		System.out.println("[Controller] UserController 초기화 완료");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			sendError(res, 400, "잘못된 요청입니다.");
			return;
		}

		String[] paths = pathInfo.split("/");
		if (paths.length < 2) {
			sendError(res, 400, "잘못된 요청업니다.");
			return;
		}

		String action = paths[1];

		switch (action) {
			case "search":
				handleSearchUser(req, res);
				break;
			case "ranking":
				handleGetRanking(req, res);
				break;
			case "my":
				handleMyPage(req, res);
				break;
			default:
				sendError(res, 404, "존재하지 않는 API입니다.");
		}
	}

	/**
	 * GET /user/search?nickname={nickname} - 닉네임으로 사용자 검색
	 */
	private void handleSearchUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String nickname = req.getParameter("nickname");

		if (nickname == null || nickname.trim().isEmpty()) {
			sendError(res, 400, "닉네임을 입력해주세요.");
			return;
		}

		if (nickname.trim().length() < 2 || nickname.trim().length() > 20) {
			sendError(res, 400, "닉네임은 2~20자로 입력해주세요.");
			return;
		}

		try {
			UserInfoVo user = searchUserByNickname(nickname.trim());

			if (user == null) {
				sendError(res, 404, "사용자를 찾을 수 없습니다.");
				return;
			}

			System.out.println("[Controller] 사용자 검색 완료: " + user.getNickname());
			sendSuccess(res, user);
		} catch (Exception e) {
			System.err.println("[Controller] 사용자 검색 실패: " + e.getMessage());
			e.printStackTrace();
			sendError(res, 500, "서버 오류가 발생했습니다.");
		}
	}

	/**
	 * GET /user/ranking?type=winRate&limit=100 - 랭킹 조회
	 */
	private void handleGetRanking(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String type = req.getParameter("type");
		String limitStr = req.getParameter("limit");

		if (type == null || type.isEmpty()) {
			type = "winRate";
		}

		if (!type.equals("winRate") && !type.equals("totalWins") && !type.equals("maxWinStreak")) {
			sendError(res, 400, "잘못된 랭킹 타입입니다. (winRate, totalWins, maxWinStreak)");
			return;
		}

		int limit = 100;
		if (limitStr != null) {
			try {
				limit = Integer.parseInt(limitStr);
				if (limit < 1 || limit > 1000) {
					limit = 100;
				}
			} catch (NumberFormatException e) {
				sendError(res, 400, "limit은 숫자여야 합니다.");
				return;
			}
		}

		try {
			List<UserInfoVo> ranking = getRanking(type, limit);

			System.out.println("[Controller] 랭킹 조회 완료: " + type + ", " + ranking.size() + "명");
			sendSuccess(res, ranking);
		} catch (Exception e) {
			System.err.println("[Controller] 랭킹 조회 실패: " + e.getMessage());
			e.printStackTrace();
			sendError(res, 500, "서버 오류가 발생했습니다.");
		}
	}

	private void handleMyPage(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("loginUserId") == null) {
			res.sendRedirect(req.getContextPath() + "/login/login.jsp");
			return;
		}

		RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/record/mypage.jsp");
		dispatcher.forward(req, res);
	}

	/**
	 * 닉네임으로 사용지 검색 (DAO 로직)
	 */
	private UserInfoVo searchUserByNickname(String nickname) throws Exception {
		String query = """
				SELECT user_id, nickname, total_win, total_lose, total_draw,
						current_streak, max_win_streak, win_rate, coin, last_game_date
				FROM user_info
				WHERE nickname = ?
			""";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, nickname);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		}

		return null;
	}

	/**
	 * 랭킹 조회 (DAO 로직)
	 */
	private List<UserInfoVo> getRanking(String type, int limit) throws Exception {
		String orderBy;

		switch (type) {
			case "totalWins":
				orderBy = "total_win DESC, win_rate DESC";
				break;
			case "maxWinStreak":
				orderBy = "max_win_streak DESC, win_rate DESC";
				break;
			case "winRate":
			default:
				orderBy = "win_rate DESC, total_win DESC";
				break;
		}

		String query = String.format("""
				SELECT user_id, nickname, total_win, total_lose, total_draw,
						current_streak, max_win_streak, win_rate, coin, last_game_date
				FROM user_info
				WHERE (total_win + total_lose + total_draw) > 0
				ORDER BY %S
				FETCH FIRST ? ROWS ONLY
			""", orderBy);

		List<UserInfoVo> ranking = new ArrayList<>();

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, limit);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					ranking.add(mapRow(rs));
				}
			}
		}

		return ranking;
	}

	/**
	 * ResultSet -> UserInfoVo 매핑
	 */
	private UserInfoVo mapRow(ResultSet rs) throws SQLException {
		return UserInfoVo.builder()
			.userId(rs.getString("user_id"))
			.nickname(rs.getString("nickname"))
			.totalWin(rs.getInt("total_win"))
			.totalLose(rs.getInt("total_lose"))
			.totalDraw(rs.getInt("total_draw"))
			.currentStreak(rs.getInt("current_streak"))
			.maxWinStreak(rs.getInt("max_win_streak"))
			.winRate(rs.getDouble("win_rate"))
			.coin(rs.getInt("coin"))
			.lastGameDate(rs.getTimestamp("last_game_date"))
			.build();
	}

	/**
	 * 성공 응답
	 */
	private void sendSuccess(HttpServletResponse res, Object data) throws IOException {
		res.setStatus(200);
		ApiResponse response = new ApiResponse(true, data, null);
		res.getWriter().write(gson.toJson(response));
	}

	/**
	 * 에러 응답
	 */
	private void sendError(HttpServletResponse res, int statusCode, String message) throws IOException {
		res.setStatus(statusCode);
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

		public ApiResponse(boolean success, Object data, String message) {
			this.success = success;
			this.data = data;
			this.message = message;
		}
	}
}
