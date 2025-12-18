package login.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import login.service.KakaoLoginService;
import login.service.KakaoLoginService.LoginResult;

@WebServlet("/kakaoCallback")
public class KakaoLoginCallbackController extends HttpServlet {

	private final KakaoLoginService kakaoLoginService = new KakaoLoginService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		String code = request.getParameter("code");

		if (code == null || code.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/login.jsp?error=missing_code");
			return;
		}

		try {
			LoginResult result = kakaoLoginService.loginWithKakao(code);

			// 세션 저장
			HttpSession session = request.getSession(true);
			session.setAttribute("loginUserId", result.getUserId());
			session.setAttribute("loginNickname", result.getNickname());

			session.setAttribute("kakaoAccessToken", result.getAccessToken());

			response.sendRedirect(request.getContextPath() + "/lobby");

		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/login.jsp?error=kakao_login_failed");
		}
	}
}
