package jwtLogin.controller.email;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import jwtLogin.dto.EmailVerifyCodeRequestDTO;
import jwtLogin.util.JsonUtil;

@WebServlet("/email/verify-code")
public class EmailVerifyCodeController extends HttpServlet {
	private final Gson gson = new Gson();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		EmailVerifyCodeRequestDTO dto = gson.fromJson(readBody(request), EmailVerifyCodeRequestDTO.class);

		if (dto == null || isBlank(dto.getEmail()) || isBlank(dto.getCode())) {
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "REQUIRED_FIELDS");
			return;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			JsonUtil.writeError(response, 401, "UNAUTHORIZED", "NO_SESSION");
			return;
		}

		String savedEmail = (String)session.getAttribute("emailVerifyTarget");
		String savedCode = (String)session.getAttribute("emailVerifyCode");
		Long expireAt = (Long)session.getAttribute("emailVerifyExpireAt");

		if (savedEmail == null || savedCode == null || expireAt == null) {
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "NO_VERIFY_REQUEST");
			return;
		}

		if (!savedEmail.equals(dto.getEmail().trim())) {
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "EMAIL_MISMATCH");
			return;
		}

		if (System.currentTimeMillis() > expireAt) {
			session.removeAttribute("emailVerifyCode"); // 만료되면 코드 제거(선택)
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "CODE_EXPIRED");
			return;
		}

		if (!savedCode.equals(dto.getCode().trim())) {
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "CODE_MISMATCH");
			return;
		}

		// ✅ 인증 성공 처리
		session.setAttribute("emailVerified", true);

		JsonUtil.writeJson(response, 200, new OkBody("EMAIL_VERIFIED"));
	}

	private String readBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = request.getReader()) {
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
		}
		return sb.toString();
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static class OkBody {
		public String message;

		public OkBody(String message) {
			this.message = message;
		}
	}
}
