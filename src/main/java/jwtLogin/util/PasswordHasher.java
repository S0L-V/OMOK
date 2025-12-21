package jwtLogin.util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

	private static final SecureRandom random = new SecureRandom();

	private static final int ITERATIONS = 120_000;
	private static final int SALT_BYTES = 16;
	private static final int KEY_LENGTH_BITS = 256;

	public static String hash(String password) {
		try {
			byte[] salt = new byte[SALT_BYTES];
			random.nextBytes(salt);

			byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);

			return "pbkdf2$" + ITERATIONS + "$"
				+ Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(hash);

		} catch (Exception e) {
			throw new RuntimeException("PASSWORD_HASH_FAIL", e);
		}
	}

	public static boolean verify(String password, String stored) {
		try {
			if (stored == null || !stored.startsWith("pbkdf2$"))
				return false;

			String[] parts = stored.split("\\$");

			if (parts.length != 4)
				return false;

			// parts[0]=pbkdf2, [1]=iter, [2]=salt, [3]=hash
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);

			byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);

			return constantTimeEquals(expected, actual);

		} catch (Exception e) {
			return false;
		}
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			return skf.generateSecret(spec).getEncoded();
		} finally {
			spec.clearPassword();
		}
	}

	private static boolean constantTimeEquals(byte[] a, byte[] b) {
		if (a == null || b == null)
			return false;
		if (a.length != b.length)
			return false;

		int result = 0;
		for (int i = 0; i < a.length; i++) {
			result |= (a[i] ^ b[i]);
		}
		return result == 0;
	}
}