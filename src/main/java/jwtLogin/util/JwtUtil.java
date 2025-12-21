package jwtLogin.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InitialContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JwtUtil {

	private static final String SECRET = loadSecret();

	private static final long ACCESS_TTL_SECONDS = 60 * 60; // 1시간
	private static final Gson gson = new Gson();

	public static String createAccessToken(String userId, String loginType, String nickname) {
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		long now = System.currentTimeMillis() / 1000;
		long exp = now + ACCESS_TTL_SECONDS;

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", userId);
		payload.put("loginType", loginType);
		payload.put("nickname", nickname);
		payload.put("iat", now);
		payload.put("exp", exp);

		String h = b64url(gson.toJson(header));
		String p = b64url(gson.toJson(payload));
		String signingInput = h + "." + p;

		String sig = hmacSha256B64Url(signingInput, SECRET);
		return signingInput + "." + sig;
	}

	public static String extractBearerToken(String authorizationHeader) {
		if (authorizationHeader == null)
			return null;

		String v = authorizationHeader.trim();
		if (v.length() < 8)
			return null; // "Bearer " 최소 길이
		if (!v.regionMatches(true, 0, "Bearer ", 0, 7))
			return null;

		String token = v.substring(7).trim();
		return token.isEmpty() ? null : token;
	}

	public static Claims verify(String token) throws JwtException {
		if (token == null || token.trim().isEmpty())
			throw new JwtException("EMPTY_TOKEN");

		String[] parts = token.split("\\.");
		if (parts.length != 3)
			throw new JwtException("INVALID_FORMAT");

		verifyHeaderIsHs256(parts[0]);

		String signingInput = parts[0] + "." + parts[1];
		String expectedSig = hmacSha256B64Url(signingInput, SECRET);
		if (!constantTimeEquals(parts[2], expectedSig))
			throw new JwtException("INVALID_SIGNATURE");

		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();

		long now = System.currentTimeMillis() / 1000;

		if (!payload.has("exp"))
			throw new JwtException("NO_EXP");
		long exp = payload.get("exp").getAsLong();
		if (now >= exp)
			throw new JwtException("TOKEN_EXPIRED");

		if (!payload.has("sub"))
			throw new JwtException("NO_SUB");

		Claims c = new Claims();
		c.userId = payload.get("sub").getAsString();
		c.loginType = payload.has("loginType") && !payload.get("loginType").isJsonNull()
			? payload.get("loginType").getAsString()
			: null;
		c.nickname = payload.has("nickname") && !payload.get("nickname").isJsonNull()
			? payload.get("nickname").getAsString()
			: null;
		c.expSec = exp;

		return c;
	}

	private static void verifyHeaderIsHs256(String headerPart) throws JwtException {
		try {
			String headerJson = new String(Base64.getUrlDecoder().decode(headerPart), StandardCharsets.UTF_8);
			JsonObject header = JsonParser.parseString(headerJson).getAsJsonObject();

			String alg = header.has("alg") ? header.get("alg").getAsString() : null;
			if (!"HS256".equals(alg))
				throw new JwtException("UNSUPPORTED_ALG");

			if (header.has("typ")) {
				String typ = header.get("typ").getAsString();
				if (!"JWT".equalsIgnoreCase(typ))
					throw new JwtException("INVALID_TYP");
			}
		} catch (JwtException e) {
			throw e;
		} catch (Exception e) {
			throw new JwtException("INVALID_HEADER");
		}
	}

	private static String b64url(String s) {
		return Base64.getUrlEncoder().withoutPadding()
			.encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private static String hmacSha256B64Url(String data, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
		} catch (Exception e) {
			throw new RuntimeException("HMAC_FAIL", e);
		}
	}

	private static boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null)
			return false;
		if (a.length() != b.length())
			return false;

		int r = 0;
		for (int i = 0; i < a.length(); i++)
			r |= (a.charAt(i) ^ b.charAt(i));
		return r == 0;
	}

	private static String loadSecret() {
		try {
			InitialContext ctx = new InitialContext();
			Object v = ctx.lookup("java:comp/env/jwt/secret");
			System.out.println("[JWT] JNDI jwt/secret = " + v);
			if (v != null) {
				String s = String.valueOf(v).trim();
				if (!s.isEmpty())
					return s;
			}
		} catch (Exception ignored) {}

		String env = System.getenv("JWT_SECRET");
		if (env != null && !env.trim().isEmpty())
			return env.trim();

		String prop = System.getProperty("JWT_SECRET");
		if (prop != null && !prop.trim().isEmpty())
			return prop.trim();

		throw new IllegalStateException(
			"JWT SECRET is missing. Set via JNDI(java:comp/env/jwt/secret) or env(JWT_SECRET) or -DJWT_SECRET=...");
	}

	public static class Claims {
		public String userId;
		public String loginType;
		public String nickname;
		public long expSec;
	}

	public static class JwtException extends Exception {
		public JwtException(String message) {
			super(message);
		}
	}
}
