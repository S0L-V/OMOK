package record.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import login.vo.UserInfoVo;

public class UserInfoDAOImpl implements UserInfoDAO {

	@Override
	public UserInfoVo getUserInfo(String userId, Connection conn) throws SQLException {
		String query = """
				SELECT user_id, nickname, total_win, total_lose, total_draw,
					current_streak, max_win_streak, win_rate, coin, last_game_date
				FROM user_info
				WHERE user_id = ?
			""";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (Exception e) {
			throw new SQLException("사용자 정보 조회 실패", e);
		}

		throw new SQLException("사용자 정보를 찾을 수 없습니다: " + userId);
	}

	@Override
	public void updateUserStats(UserInfoVo userInfo, Connection conn) throws SQLException {
		String query = """
				UPDATE user_info
				SET total_win = ?,
					total_lose = ?,
					total_draw = ?,
					current_streak = ?,
					max_win_streak = ?,
					win_rate = ?,
					last_game_date = ?
				WHERE user_id = ?
			""";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, userInfo.getTotalWin());
			pstmt.setInt(2, userInfo.getTotalLose());
			pstmt.setInt(3, userInfo.getTotalDraw());
			pstmt.setInt(4, userInfo.getCurrentStreak());
			pstmt.setInt(5, userInfo.getMaxWinStreak());
			pstmt.setDouble(6, userInfo.getWinRate());
			pstmt.setTimestamp(7, userInfo.getLastGameDate());
			pstmt.setString(8, userInfo.getUserId());

			int updated = pstmt.executeUpdate();
			if (updated == 0) {
				throw new SQLException("사용자 통계 업데이트 실패: " + userInfo.getUserId());
			}

			System.out.println("[DAO] : " + userInfo.getUserId() + " 통걔 업데이트 완료");
		} catch (Exception e) {
			System.out.println("[DAO] updateUserStats 실패");
			throw new SQLException("사용자 통계 업데이트 실패", e);
		}
	}

	private UserInfoVo mapRow(ResultSet rs) throws SQLException {
		return UserInfoVo.builder()
			.userId(rs.getString("user_id"))
			.nickname(rs.getString("nickname"))
			.totalWin(rs.getInt("total_win"))
			.totalLose(rs.getInt("total_lose"))
			.totalDraw(rs.getInt("total_draw"))
			.currentStreak(rs.getInt("current_streak"))
			.maxWinStreak(rs.getInt("max_win_streak"))
			.winRate(rs.getInt("win_rate"))
			.coin(rs.getInt("coin"))
			.lastGameDate(rs.getTimestamp("last_game_date"))
			.build();
	}
}
