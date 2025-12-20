package jwtLogin.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class JsonUtil {

	private static final Gson gson = new Gson();

	public static <T> void writeJson(HttpServletResponse response, int status, T body) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json; charset=UTF-8");

		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");

		response.getWriter().write(gson.toJson(body));
		response.getWriter().flush();
	}

	public static void writeError(HttpServletResponse response, int status, String message, String code)
		throws IOException {
		writeJson(response, status, new ErrorBody(message, code));
	}

	public static class ErrorBody {
		public String message;
		public String code;

		public ErrorBody(String message, String code) {
			this.message = message;
			this.code = code;

		}
	}
}