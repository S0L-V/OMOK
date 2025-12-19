package login.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import login.config.KakaoConfig;
import login.dto.KakaoTokenResponseDTO;
import login.dto.KakaoUserInfoDTO;

public class KakaoOAuthService {

	public KakaoTokenResponseDTO requestToken(String code) throws Exception {

		String tokenUrl = "https://kauth.kakao.com/oauth/token";

		URL url = new URL(tokenUrl);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		conn.setRequestMethod("POST");
		conn.setDoOutput(true); // POST body 사용

		// POST body 파라미터 구성
		StringBuilder params = new StringBuilder();
		params.append("grant_type=authorization_code");
		params.append("&client_id=").append(URLEncoder.encode(KakaoConfig.restApiKey(), "UTF-8"));
		params.append("&redirect_uri=").append(URLEncoder.encode(KakaoConfig.redirectUri(), "UTF-8"));
		params.append("&code=").append(URLEncoder.encode(code, "UTF-8"));

		// client_secret을 쓰는 경우만 추가
		if (KakaoConfig.clientSecret() != null && !KakaoConfig.clientSecret().isBlank()) {
			params.append("&client_secret=")
				.append(URLEncoder.encode(KakaoConfig.clientSecret(), "UTF-8"));
		}

		// body 전송
		try (OutputStream os = conn.getOutputStream()) {
			os.write(params.toString().getBytes());
			os.flush();
		}

		// 응답 읽기
		BufferedReader br;
		if (conn.getResponseCode() == 200) {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}

		StringBuilder response = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			response.append(line);
		}
		br.close();

		// json 파싱
		JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

		KakaoTokenResponseDTO dto = new KakaoTokenResponseDTO();
		dto.setAccessToken(getAsString(json, "access_token"));
		dto.setTokenType(getAsString(json, "token_type"));
		dto.setExpiresIn(getAsInt(json, "expires_in"));
		dto.setRefreshToken(getAsString(json, "refresh_token"));
		dto.setRefreshTokenExpiresIn(getAsInt(json, "refresh_token_expires_in"));
		dto.setScope(getAsString(json, "scope"));

		return dto;
	}

	public KakaoUserInfoDTO requestUserInfo(String accessToken) throws Exception {

		String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 요청헤더 보내는 주소

		URL url = new URL(userInfoUrl);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		BufferedReader br;
		if (conn.getResponseCode() == 200) {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}

		StringBuilder response = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			response.append(line);
		}
		br.close();

		JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

		// user 정보 json
		JsonObject kakaoAccount = json.getAsJsonObject("kakao_account");

		String email = null;
		String nickname = null;

		if (kakaoAccount != null) {
			if (kakaoAccount.has("email")) {
				email = kakaoAccount.get("email").getAsString();
			}

			JsonObject profile = kakaoAccount.getAsJsonObject("profile");
			if (profile != null && profile.has("nickname")) {
				nickname = profile.get("nickname").getAsString();
			}
		}

		KakaoUserInfoDTO dto = new KakaoUserInfoDTO();
		dto.setEmail(email);
		dto.setNickname(nickname);

		return dto;
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
