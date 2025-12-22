package user.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import friend.dto.FriendDTO;
import friend.service.FriendService;
import friend.service.FriendServiceImpl;
import login.vo.UserInfoVo;
import record.dto.GameResultDTO;
import util.DB;

@WebServlet("/user/my")
public class MyPageController extends HttpServlet {

	// 친구 목록 가져오기 위해 서비스 추가
	private FriendService friendService;

	@Override
	public void init() throws ServletException {
		try {
			this.friendService = new FriendServiceImpl();
		} catch (Exception e) {
			throw new ServletException("FriendService 초기화 실패");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		String userId = (session != null) ? (String)session.getAttribute("loginUserId") : null;

		if (userId == null) {
			res.sendRedirect(req.getContextPath() + "/login/login.jsp");
			return;
		}

		try {
			// 1. 내 정보 & 전적 가져오기
			UserInfoVo userInfo = getUserInfo(userId);
			List<GameResultDTO> gameHistory = getGameHistory(userId);

			// 2. [추가] 내 친구 목록 가져오기
			List<FriendDTO> myFriends = friendService.getMyFriends(userId);

			// 3. 데이터 담기
			req.setAttribute("userInfo", userInfo);
			req.setAttribute("gameHistory", gameHistory);
			req.setAttribute("myFriends", myFriends); // JSP로 친구 목록 전달

			RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/record/mypage.jsp");
			dispatcher.forward(req, res);

		} catch (Exception e) {
			e.printStackTrace();
			res.sendError(500, "DB 조회 실패");
		}
	}

	// ... (getUserInfo, getGameHistory 메소드는 기존과 동일하므로 생략) ...
	// 기존 getUserInfo, getGameHistory 메소드 그대로 유지하세요!
	private UserInfoVo getUserInfo(String userId) throws Exception {
		String sql = "SELECT * FROM user_info WHERE user_id = ?";
		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
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
			}
		}
		return null;
	}

	private List<GameResultDTO> getGameHistory(String userId) throws Exception {
		List<GameResultDTO> list = new ArrayList<>();
		String sql = "SELECT * FROM game_result WHERE user_id = ? ORDER BY finished_at DESC FETCH FIRST 10 ROWS ONLY";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					GameResultDTO dto = GameResultDTO.builder()
						.id(rs.getString("id"))
						.gameId(rs.getString("game_id"))
						.roomId(rs.getString("room_id"))
						.userId(rs.getString("user_id"))
						.stoneColor(rs.getString("stone_color"))
						.gameResult(rs.getString("game_result"))
						.playType(rs.getString("play_type"))
						.finishedAt(rs.getTimestamp("finished_at"))
						.build();
					list.add(dto);
				}
			}
		}
		return list;
	}
}