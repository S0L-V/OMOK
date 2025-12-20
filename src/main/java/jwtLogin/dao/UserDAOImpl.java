package jwtLogin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jwtLogin.vo.UserVo;
import util.DB;
import util.Uuid;

public class UserDAOImpl implements UserDAO {

	@Override
	public UserVo findByEmail(String email) throws Exception {
		String sql = """
			      SELECT u.id, u.email, u.pwd, u.login_type, ui.nickname
			        FROM users u
			        JOIN user_info ui ON ui.user_id = u.id
			      WHERE u.email = ?
			""";

		try (Connection conn = DB.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				return UserVo.builder()
					.id(rs.getString("id"))
					.email(rs.getString("email"))
					.pwd(rs.getString("pwd"))
					.loginType(rs.getString("login_type"))
					.nickname(rs.getString("nickname"))
					.build();
			}
		}
	}

	@Override
	public boolean existsEmail(String email) throws Exception {
		String sql = """
			SELECT 1 FROM users WHERE email = ?
			""";
		try (Connection conn = DB.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	@Override
	public boolean existsNickname(String nickname) throws Exception {
		String sql = """
			 	SELECT 1 FROM user_info WHERE nickname = ?
			""";
		try (Connection conn = DB.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, nickname);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	@Override
	public UserVo insertNormalUser(String email, String pwdHashed, String nickname) throws Exception {
		String userId = Uuid.generate();

		String sqlUser = """
			INSERT INTO users (id, email, pwd, login_type, created_at)
			VALUES (?, ?, ?, '0', SYSTIMESTAMP)
			""";

		String sqlInfo = """
			INSERT INTO user_info (user_id, nickname, total_win, total_lose, total_draw, current_streak, max_win_streak, win_rate, coin)
			VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0)
			""";
		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement ps1 = conn.prepareStatement(sqlUser);
				PreparedStatement ps2 = conn.prepareStatement(sqlInfo)) {

				ps1.setString(1, userId);
				ps1.setString(2, email);
				ps1.setString(3, pwdHashed);
				ps1.executeUpdate();

				ps2.setString(1, userId);
				ps2.setString(2, nickname);
				ps2.executeUpdate();

				conn.commit();

				return UserVo.builder()
					.id(userId)
					.email(email)
					.pwd(pwdHashed)
					.loginType("0")
					.nickname(nickname)
					.build();

			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		}
	}
}