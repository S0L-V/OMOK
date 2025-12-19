package login.service;

import java.sql.Connection;
import java.util.Random;
import java.util.UUID;

import login.dao.LoginUserDAO;
import login.dao.LoginUserDAOImpl;
import login.dao.LoginUserInfoDAO;
import login.dao.LoginUserInfoDAOImpl;
import login.dto.KakaoTokenResponseDTO;
import login.dto.KakaoUserInfoDTO;
import login.vo.UserInfoVo;
import login.vo.UserVo;
import util.DB;

public class KakaoLoginService {

	private final KakaoOAuthService kakaoOAuthService = new KakaoOAuthService();
	private final LoginUserDAO userDao = new LoginUserDAOImpl();
	private final LoginUserInfoDAO userInfoDao = new LoginUserInfoDAOImpl();

	public LoginResult loginWithKakao(String code) throws Exception {

		KakaoTokenResponseDTO token = kakaoOAuthService.requestToken(code);

		String accessToken = token.getAccessToken();

		KakaoUserInfoDTO kakaoUser = kakaoOAuthService.requestUserInfo(accessToken);

		if (kakaoUser.getEmail() == null || kakaoUser.getEmail().isBlank()) {
			throw new IllegalStateException("카카오 이메일 동의가 필요합니다. (account_email scope)");
		}

		String email = kakaoUser.getEmail();

		String rawNickname = (kakaoUser.getNickname() == null || kakaoUser.getNickname().isBlank())
			? "kakao"
			: kakaoUser.getNickname();

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try {
				UserVo existingUser = userDao.findByEmail(conn, email);

				String userId;

				if (existingUser == null) {
					userId = UUID.randomUUID().toString();

					String dummyPwd = "KAKAO_" + UUID.randomUUID();

					UserVo newUser = new UserVo();
					newUser.setId(userId);
					newUser.setEmail(email);
					newUser.setPwd(dummyPwd);
					newUser.setLoginType("1"); // 카카오=1, 일반=2

					userDao.insert(conn, newUser);

					// user_info.nickname UNIQUE + 10자 제한 대응
					String uniqueNickname = makeUniqueNickname(conn, rawNickname);

					UserInfoVo info = new UserInfoVo();
					info.setUserId(userId);
					info.setNickname(uniqueNickname);

					userInfoDao.insert(conn, info);

				} else {
					userId = existingUser.getId();

					UserInfoVo info = userInfoDao.findByUserId(conn, userId);
					if (info == null) {
						String uniqueNickname = makeUniqueNickname(conn, rawNickname);

						UserInfoVo newInfo = new UserInfoVo();
						newInfo.setUserId(userId);
						newInfo.setNickname(uniqueNickname);

						userInfoDao.insert(conn, newInfo);
					}
				}

				UserInfoVo dbInfo = userInfoDao.findByUserId(conn, userId);
				String nickname = (dbInfo != null && dbInfo.getNickname() != null)
					? dbInfo.getNickname()
					: truncateNickname(rawNickname);

				conn.commit();
				return new LoginResult(userId, nickname, accessToken);

			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
	}

	private String makeUniqueNickname(Connection conn, String raw) throws Exception {
		String base = truncateNickname(raw);
		if (base.isBlank())
			base = "kakao";

		// 1) 중복 아니면 그대로 사용
		UserInfoVo exists = userInfoDao.findByNickname(conn, base);
		if (exists == null)
			return base;

		Random r = new Random();
		for (int i = 0; i < 1000; i++) {
			String suffix = String.valueOf(r.nextInt(100)); // 0~99
			String candidate = truncateNickname(base + suffix);

			if (userInfoDao.findByNickname(conn, candidate) == null) {
				return candidate;
			}
		}

		return truncateNickname(base + (System.currentTimeMillis() % 1000));
	}

	private String truncateNickname(String nickname) {
		if (nickname == null)
			return "";
		nickname = nickname.trim();
		return nickname;
	}

	public static class LoginResult {
		private final String userId;
		private final String nickname;
		private final String accessToken;

		public LoginResult(String userId, String nickname, String accessToken) {
			this.userId = userId;
			this.nickname = nickname;
			this.accessToken = accessToken;
		}

		public String getUserId() {
			return userId;
		}

		public String getNickname() {
			return nickname;
		}

		public String getAccessToken() {
			return accessToken;
		}
	}
}
