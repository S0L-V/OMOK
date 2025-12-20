package jwtLogin.controller;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import jwtLogin.dto.LoginRequestDTO;
import jwtLogin.dto.ResponseDTO;
import jwtLogin.service.JwtService;
import jwtLogin.service.JwtServiceImpl;
import jwtLogin.service.JwtServiceImpl.AuthException;
import jwtLogin.util.JsonUtil;

@WebServlet("/noamlLogin")
public class JwtLoginController extends HttpServlet {

	private final JwtService jwtService = new JwtServiceImpl();
	private final Gson gson = new Gson();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			LoginRequestDTO dto = gson.fromJson(readBody(request), LoginRequestDTO.class);

			if (dto == null || isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
				JsonUtil.writeError(response, 400, "BAD_REQUEST", "REQUIRED_FIELDS");
				return;
			}

			ResponseDTO out = jwtService.loginNormal(dto.getEmail().trim(), dto.getPassword());

			HttpSession session = request.getSession(true);
			session.setAttribute("accessToken", out.getAccessToken());

			JsonUtil.writeJson(response, 200, out);

		} catch (AuthException e) {
			JsonUtil.writeError(response, 401, "AUTH_ERROR", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			JsonUtil.writeError(response, 500, "SERVER_ERROR", "LOGIN_FAIL");
		}
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
}
