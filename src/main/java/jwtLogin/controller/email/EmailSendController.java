package jwtLogin.controller.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.SecureRandom;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import jwtLogin.dto.EmailSendCodeRequestDTO;
import jwtLogin.util.EmailSender;
import jwtLogin.util.JsonUtil;

@WebServlet("/email/send-code")
public class EmailSendController extends HttpServlet {

	private final Gson gson = new Gson();
	private static final SecureRandom random = new SecureRandom();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		EmailSendCodeRequestDTO dto = gson.fromJson(readBody(request), EmailSendCodeRequestDTO.class);

		if (dto == null || isBlank(dto.getEmail())) {
			JsonUtil.writeError(response, 400, "BAD_REQUEST", "EMAIL_REQUIRED");
			return;
		}

		String email = dto.getEmail().trim();
		String code = String.format("%06d", random.nextInt(1_000_000));
		HttpSession session = request.getSession(true);
		session.setAttribute("emailVerifyTarget", email);
		session.setAttribute("emailVerifyCode", code);
		session.setAttribute("emailVerifyExpireAt", System.currentTimeMillis() + (5 * 60 * 1000)); // 5분
		session.setAttribute("emailVerified", false); // 아직 인증 전

		try {
			EmailSender.sendVerifyCode(getServletContext(), email, code);
		} catch (Exception e) {
			e.printStackTrace();
			JsonUtil.writeError(response, 500, "SMTP_ERROR", "SEND_FAILED");
			return;
		}

		// ✅ 성공 응답(간단 텍스트)
		JsonUtil.writeJson(response, 200, new OkBody("CODE_SENT"));
	}

	// ------------------------------
	// 내부 유틸 (JwtSignupController 스타일 맞춤)
	// ------------------------------
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

	// ✅ 응답 바디 클래스(간단)
	private static class OkBody {
		public String message;

		public OkBody(String message) {
			this.message = message;
		}
	}
}
