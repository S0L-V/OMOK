package login.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KakaoConfig {

	private static final Properties props = new Properties();

	static {
		// ✅ application.properties를 클래스패스에서 읽는다.
		try (InputStream in = KakaoConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (in == null) {
				throw new RuntimeException(
					"application.properties를 클래스패스에서 찾지 못했습니다. " +
						"파일 위치를 src(소스폴더) 아래에 두었는지 확인하세요.");
			}
			props.load(in);
		} catch (IOException e) {
			throw new RuntimeException("application.properties 로드 실패", e);
		}
	}

	// ✅ properties 키 이름에 맞게 읽는다 (너 파일 기준)
	public static String restApiKey() {
		return props.getProperty("KAKAO.REST_API_KEY");
	}

	public static String clientSecret() {
		return props.getProperty("KAKAO.CLIENT_SECRET", "");
	}

	public static String redirectUri() {
		return props.getProperty("KAKAO.REDIRECT_URI");
	}
}
