package jwtLogin.service;

import jwtLogin.dao.UserDAO;
import jwtLogin.dao.UserDAOImpl;
import jwtLogin.dto.ResponseDTO;
import jwtLogin.util.JwtUtil;
import jwtLogin.util.PasswordHasher;
import jwtLogin.vo.UserVo;

public class JwtServiceImpl implements JwtService {

	private final UserDAO userDao = new UserDAOImpl();

	@Override
	public ResponseDTO signupNormal(String email, String password, String nickname) throws Exception {
		if (userDao.existsEmail(email)) {
			throw new AuthException("EMAIL_ALREADY_EXISTS");
		}
		if (userDao.existsNickname(nickname)) {
			throw new AuthException("NICKNAME_ALREADY_EXISTS");
		}

		String hashed = PasswordHasher.hash(password);
		UserVo created = userDao.insertNormalUser(email, hashed, nickname);

		return issueToken(created.getId(), created.getEmail(), created.getLoginType(), created.getNickname());
	}

	@Override
	public ResponseDTO loginNormal(String email, String password) throws Exception {
		UserVo user = userDao.findByEmail(email);
		if (user == null)
			throw new AuthException("INVALID_CREDENTIALS");

		// 일반 로그인인데 카카오 계정이면 막기(정책 선택)
		if (!"0".equals(user.getLoginType())) {
			throw new AuthException("NOT_NORMAL_ACCOUNT");
		}

		if (!PasswordHasher.verify(password, user.getPwd())) {
			throw new AuthException("INVALID_CREDENTIALS");
		}

		return issueToken(user.getId(), user.getEmail(), user.getLoginType(), user.getNickname());
	}

	@Override
	public ResponseDTO issueToken(String userId, String email, String loginTypeDb, String nickname) {

		String loginTypeStr = "1".equals(loginTypeDb) ? "KAKAO" : "NORMAL";

		String accessToken = JwtUtil.createAccessToken(userId, loginTypeStr, nickname);

		return ResponseDTO.builder()
			.accessToken(accessToken)
			.user(ResponseDTO.User.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.loginType(loginTypeStr)
				.build())
			.build();
	}

	// 서비스 계층 예외(컨트롤러에서 잡아서 status/응답코드로 변환)
	public static class AuthException extends Exception {
		public AuthException(String code) {
			super(code);
		}
	}
}
