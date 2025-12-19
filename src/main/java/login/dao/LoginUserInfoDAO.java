package login.dao;

import java.sql.Connection;
import java.sql.SQLException;

import login.vo.UserInfoVo;

public interface LoginUserInfoDAO {

	UserInfoVo findByUserId(Connection conn, String userId) throws SQLException;

	UserInfoVo findByNickname(Connection conn, String nickname) throws SQLException;

	int insert(Connection conn, UserInfoVo userInfoVo) throws SQLException;

}
