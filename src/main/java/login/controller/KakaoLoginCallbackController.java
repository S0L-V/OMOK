package login.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jwtLogin.dto.ResponseDTO;
import jwtLogin.service.JwtService;
import jwtLogin.service.JwtServiceImpl;
import jwtLogin.util.JsonUtil;
import login.service.KakaoLoginService;
import login.service.KakaoLoginService.LoginResult;

@WebServlet("/kakaoCallback")
public class KakaoLoginCallbackController extends HttpServlet {

	private final KakaoLoginService kakaoLoginService = new KakaoLoginService();
	private final JwtService jwtService = new JwtServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		try {
			String code = request.getParameter("code");
			if (code == null) {
				JsonUtil.writeError(response, 400, "BAD_REQUEST", "NO_AUTH_CODE");
				return;
			}

			LoginResult result = kakaoLoginService.loginWithKakao(code);

			ResponseDTO jwtResponse = jwtService.issueToken(
				result.getUserId(),
				result.getEmail(),
				"1",
				result.getNickname());

			// 4️⃣ JWT를 세션에 저장 (로비에서 사용 가능)
			HttpSession session = request.getSession(true);
			session.setAttribute("loginUserId", result.getUserId());
			session.setAttribute("accessToken", jwtResponse.getAccessToken());
			session.setAttribute("loginType", "1");
			session.setAttribute("loginNickname", result.getNickname());

			// ✅ 5️⃣ 로그인 성공 → 로비로 이동
			response.sendRedirect(request.getContextPath() + "/lobby");

		} catch (Exception e) {
			e.printStackTrace();
			JsonUtil.writeError(response, 500, "SERVER_ERROR", "KAKAO_LOGIN_FAIL");
		}
	}
}