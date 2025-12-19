package record.dao;

import java.sql.Connection;
import java.sql.SQLException;

import login.vo.UserInfoVo;

public interface UserInfoDAO {

	/**
	 * 사용자 정보 조회
	 */
	UserInfoVo getUserInfo(String userId, Connection conn) throws SQLException;

	/**
	 * 사용자 통계 업데이트
	 */
	void updateUserStats(UserInfoVo userInfo, Connection conn) throws SQLException;
}
