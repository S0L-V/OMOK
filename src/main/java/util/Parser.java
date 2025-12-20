package util;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public final class Parser {

	private static final Gson gson = new Gson();

	private Parser() {}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> parse(String json) {
		return gson.fromJson(json, Map.class);
	}

	public static String asString(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	public static String toJson(Object o) {
		return gson.toJson(o);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> asMap(Object o) {
		if (o instanceof Map)
			return (Map<String, Object>)o;
		return new HashMap<>();
	}
}
