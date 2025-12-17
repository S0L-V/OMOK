// 파일명: src/main/java/util/AppConfig.java
package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppConfig {

	private static final Properties props = new Properties();

	static {

		try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (is == null) {
				throw new RuntimeException("application.properties를 classpath에서 찾지 못했습니다. (resources에 있는지 확인)");
			}
			props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException("application.properties 로딩 실패", e);
		}
	}

	public static String get(String key) {
		String v = props.getProperty(key);
		if (v == null || v.isBlank()) {
			throw new IllegalStateException("application.properties에 '" + key + "' 값이 없습니다.");
		}
		return v.trim();
	}
}
