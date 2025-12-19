package login.dao;

import java.sql.Connection;
import java.sql.SQLException;

import login.vo.UserVo;

public interface LoginUserDAO {

	UserVo findByEmail(Connection conn, String email) throws SQLException;

	UserVo findById(Connection conn, String id) throws SQLException;

	int insert(Connection conn, UserVo userVo) throws SQLException;

}