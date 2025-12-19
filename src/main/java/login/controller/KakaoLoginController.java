package login.controller;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import login.config.KakaoConfig;

@WebServlet("/login/kakao")
public class KakaoLoginController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		String restApiKey  = KakaoConfig.restApiKey();
        String redirectUri = KakaoConfig.redirectUri();
        
        if (restApiKey == null || restApiKey.isBlank()) {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().println("KAKAO.REST_API_KEY 값이 null/빈값입니다. application.properties 위치/키이름을 확인하세요.");
            return;
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().println("KAKAO.REDIRECT_URI 값이 null/빈값입니다. application.properties 위치/키이름을 확인하세요.");
            return;
        }

		// 카카오 인가 요청 url
		StringBuilder sb = new StringBuilder("https://kauth.kakao.com/oauth/authorize");
		sb.append("?response_type=code");
		sb.append("&client_id=").append(URLEncoder.encode(KakaoConfig.restApiKey(), "UTF-8"));
		sb.append("&redirect_uri=").append(URLEncoder.encode(KakaoConfig.redirectUri(), "UTF-8"));
		sb.append("&scope=").append(URLEncoder.encode("account_email,profile_nickname", "UTF-8"));

		response.sendRedirect(sb.toString());
	}
}
