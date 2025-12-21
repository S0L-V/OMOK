package jwtLogin.dao;

import jwtLogin.vo.UserVo;

public interface UserDAO {
	UserVo findByEmail(String email) throws Exception;

	boolean existsEmail(String email) throws Exception;

	boolean existsNickname(String nickname) throws Exception;

	UserVo insertNormalUser(String email, String pwdHashed, String nickname) throws Exception;

}
