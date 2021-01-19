package edu.intech.meetings.utils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {

	private final static String SALT = "EqdmPh53c9x33EygXpTpcoJvc4VXLK";
	private static final int ITERATIONS = 10000;
	private static final int KEY_LENGTH = 256;

	public static byte[] hash(final char[] password, final byte[] salt) {
		final PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		Arrays.fill(password, Character.MIN_VALUE);
		try {
			final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			return skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
		} finally {
			spec.clearPassword();
		}
	}

	public static String generateSecurePassword(final String password) {
		String returnValue = null;
		final byte[] securePassword = hash(password.toCharArray(), SALT.getBytes());

		returnValue = Base64.getEncoder().encodeToString(securePassword);

		return returnValue;
	}

	public static boolean verifyUserPassword(final String providedPassword,
			final String securedPassword) {
		boolean returnValue = false;

		// Generate New secure password with the same salt
		final String newSecurePassword = generateSecurePassword(providedPassword);

		// Check if two passwords are equal
		returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);

		return returnValue;
	}
}