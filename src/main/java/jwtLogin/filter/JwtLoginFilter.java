package jwtLogin.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jwtLogin.util.JsonUtil;
import jwtLogin.util.JwtUtil;

public class JwtLoginFilter implements Filter {

	private boolean isWhitelisted(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String ctx = req.getContextPath();
		String path = uri.substring(ctx.length());

		return path.startsWith("/login")
			|| path.startsWith("/normalLogin")
			|| path.startsWith("/login/kakao")
			|| path.startsWith("/static")
			|| path.equals("/lobby");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			chain.doFilter(req, res);
			return;
		}

		if (isWhitelisted(request)) {
			chain.doFilter(req, res);
			return;
		}

		String token = null;

		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring("Bearer ".length()).trim();
		}

		if (token == null) {
			HttpSession session = request.getSession(false);
			System.out.println("[JWT] path=" + request.getRequestURI());
			System.out.println("[JWT] hasSession=" + (session != null));
			System.out.println("[JWT] hasAuthHeader=" + (authHeader != null));
			System.out.println(
				"[JWT] sessionAccessToken=" + (session != null ? session.getAttribute("accessToken") != null : false));
			if (session != null) {
				token = (String)session.getAttribute("accessToken");
			}
		}

		if (token == null) {
			JsonUtil.writeError(response, 401, "UNAUTHORIZED", "NO_BEARER_TOKEN");
			return;
		}

		try {
			JwtUtil.Claims claims = JwtUtil.verify(token);

			request.setAttribute("authUserId", claims.userId);
			request.setAttribute("authLoginType", claims.loginType);
			request.setAttribute("authNickname", claims.nickname);

			chain.doFilter(req, res);

		} catch (JwtUtil.JwtException e) {
			JsonUtil.writeError(response, 401, "UNAUTHORIZED", e.getMessage());
		}
	}
}