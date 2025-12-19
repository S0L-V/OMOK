package login.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import login.config.KakaoConfig;
import login.dto.KakaoTokenResponseDTO;
import login.dto.KakaoUserInfoDTO;

public class KakaoOAuthService {

	public KakaoTokenResponseDTO requestToken(String code) throws Exception {

		String tokenUrl = "https://kauth.kakao.com/oauth/token";
		HttpURLConnection conn = null;

		try {
			URL url = new URL(tokenUrl);
			conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			// POST body 파라미터 구성
			StringBuilder params = new StringBuilder();
			params.append("grant_type=authorization_code");
			params.append("&client_id=").append(URLEncoder.encode(KakaoConfig.restApiKey(), "UTF-8"));
			params.append("&redirect_uri=").append(URLEncoder.encode(KakaoConfig.redirectUri(), "UTF-8"));
			params.append("&code=").append(URLEncoder.encode(code, "UTF-8"));

			if (KakaoConfig.clientSecret() != null && !KakaoConfig.clientSecret().isBlank()) {
				params.append("&client_secret=")
					.append(URLEncoder.encode(KakaoConfig.clientSecret(), "UTF-8"));
			}

			try (OutputStream os = conn.getOutputStream()) {
				os.write(params.toString().getBytes(StandardCharsets.UTF_8));
				os.flush();
			}

			int status = conn.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				String errBody = readAll(conn.getErrorStream());
				throw new RuntimeException("Kakao token API failed. status=" + status + ", body=" + errBody);
			}

			String body = readAll(conn.getInputStream());

			JsonObject json = JsonParser.parseString(body).getAsJsonObject();

			KakaoTokenResponseDTO dto = new KakaoTokenResponseDTO();
			dto.setAccessToken(getAsString(json, "access_token"));
			dto.setTokenType(getAsString(json, "token_type"));
			dto.setExpiresIn(getAsInt(json, "expires_in"));
			dto.setRefreshToken(getAsString(json, "refresh_token"));
			dto.setRefreshTokenExpiresIn(getAsInt(json, "refresh_token_expires_in"));
			dto.setScope(getAsString(json, "scope"));

			return dto;

		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	public KakaoUserInfoDTO requestUserInfo(String accessToken) throws Exception {

		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
		HttpURLConnection conn = null; // 

		try {
			URL url = new URL(userInfoUrl);
			conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

			int status = conn.getResponseCode();

			// 예외처리
			if (status != HttpURLConnection.HTTP_OK) {
				String errBody = readAll(conn.getErrorStream());
				throw new RuntimeException("Kakao userInfo API failed. status=" + status + ", body=" + errBody);
			}

			String body = readAll(conn.getInputStream());

			JsonObject json = JsonParser.parseString(body).getAsJsonObject();
			JsonObject kakaoAccount = json.has("kakao_account") && json.get("kakao_account").isJsonObject()
				? json.getAsJsonObject("kakao_account")
				: null;

			String email = null;
			String nickname = null;

			if (kakaoAccount != null) {
				if (kakaoAccount.has("email") && !kakaoAccount.get("email").isJsonNull()) {
					email = kakaoAccount.get("email").getAsString();
				}

				JsonObject profile = kakaoAccount.has("profile") && kakaoAccount.get("profile").isJsonObject()
					? kakaoAccount.getAsJsonObject("profile")
					: null;

				if (profile != null && profile.has("nickname") && !profile.get("nickname").isJsonNull()) {
					nickname = profile.get("nickname").getAsString();
				}
			}

			KakaoUserInfoDTO dto = new KakaoUserInfoDTO();
			dto.setEmail(email);
			dto.setNickname(nickname);

			return dto;

		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	private String readAll(InputStream is) throws Exception {
		if (is == null)
			return "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			return sb.toString();
		}
	}

	// 파싱
	private String getAsString(JsonObject json, String key) {
		return json.has(key) && !json.get(key).isJsonNull()
			? json.get(key).getAsString()
			: null;
	}

	private Integer getAsInt(JsonObject json, String key) {
		return json.has(key) && !json.get(key).isJsonNull()
			? json.get(key).getAsInt()
			: null;
	}
}
