package login.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import login.vo.UserInfoVo;

public class LoginUserInfoDAOImpl implements LoginUserInfoDAO {

	@Override
	public UserInfoVo findByUserId(Connection conn, String userId) throws SQLException {

		String sql = """
			SELECT user_id, nickname, total_win, total_lose, total_draw,
				   current_streak, max_win_streak, win_rate, coin, last_game_date
			FROM user_info
			WHERE user_id = ?
			""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				UserInfoVo vo = new UserInfoVo();
				vo.setUserId(rs.getString("user_id"));
				vo.setNickname(rs.getString("nickname"));
				vo.setTotalWin(rs.getInt("total_win"));
				vo.setTotalLose(rs.getInt("total_lose"));
				vo.setTotalDraw(rs.getInt("total_draw"));
				vo.setCurrentStreak(rs.getInt("current_streak"));
				vo.setMaxWinStreak(rs.getInt("max_win_streak"));
				vo.setWinRate(rs.getDouble("win_rate"));
				vo.setCoin(rs.getInt("coin"));
				vo.setLastGameDate(rs.getTimestamp("last_game_date"));
				return vo;
			}
		}
	}

	@Override
	public UserInfoVo findByNickname(Connection conn, String nickname) throws SQLException {

		String sql = """
			SELECT user_id, nickname, total_win, total_lose, total_draw,
			       current_streak, max_win_streak, win_rate, coin, last_game_date
			FROM user_info
			WHERE nickname = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, nickname);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				UserInfoVo vo = new UserInfoVo();
				vo.setUserId(rs.getString("user_id"));
				vo.setNickname(rs.getString("nickname"));
				vo.setTotalWin(rs.getInt("total_win"));
				vo.setTotalLose(rs.getInt("total_lose"));
				vo.setTotalDraw(rs.getInt("total_draw"));
				vo.setCurrentStreak(rs.getInt("current_streak"));
				vo.setMaxWinStreak(rs.getInt("max_win_streak"));
				vo.setWinRate(rs.getDouble("win_rate"));
				vo.setCoin(rs.getInt("coin"));
				vo.setLastGameDate(rs.getTimestamp("last_game_date"));
				return vo;
			}
		}
	}

	@Override
	public int insert(Connection conn, UserInfoVo userInfoVo) throws SQLException {

		String sql = """
			INSERT INTO user_info (user_id, nickname)
			VALUES (?, ?)
			""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userInfoVo.getUserId());
			ps.setString(2, userInfoVo.getNickname());
			return ps.executeUpdate();
		}
	}
}
