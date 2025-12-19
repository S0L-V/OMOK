package login.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import login.vo.UserVo;

public class LoginUserDAOImpl implements LoginUserDAO {

	@Override
	public UserVo findByEmail(Connection conn, String email) throws SQLException {
		String sql = """
			SELECT id, email, pwd, created_at, login_type
			FROM users
			WHERE email = ?
			""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				UserVo vo = new UserVo();
				vo.setId(rs.getString("id"));
				vo.setEmail(rs.getString("email"));
				vo.setPwd(rs.getString("pwd"));
				vo.setCreatedAt(rs.getTimestamp("created_at"));
				vo.setLoginType(rs.getString("login_type"));
				return vo;
			}
		}
	}

	@Override
	public UserVo findById(Connection conn, String id) throws SQLException {
		String sql = """
			SELECT id, email, pwd, created_at, login_type
			FROM users
			WHERE id = ?
			""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				UserVo vo = new UserVo();
				vo.setId(rs.getString("id"));
				vo.setEmail(rs.getString("email"));
				vo.setPwd(rs.getString("pwd"));
				vo.setCreatedAt(rs.getTimestamp("created_at"));
				vo.setLoginType(rs.getString("login_type"));
				return vo;
			}
		}
	}

	@Override
	public int insert(Connection conn, UserVo userVo) throws SQLException {

		String sql = "INSERT INTO users (id, email, pwd, login_type) " +
			"VALUES (?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userVo.getId());
			ps.setString(2, userVo.getEmail());
			ps.setString(3, userVo.getPwd());
			ps.setString(4, "1");

			return ps.executeUpdate();
		}
	}
}