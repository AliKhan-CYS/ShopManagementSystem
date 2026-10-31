package com.shop.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordSecurity {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 310_000;
    private static final int MIN_VERIFY_ITERATIONS = 10_000;
    private static final int MAX_VERIFY_ITERATIONS = 1_000_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordSecurity() {
    }

    public static String hash(String password) {
        if (password == null || password.length() > 1024) {
            throw new IllegalArgumentException("Password is missing or too long");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_BITS);

        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String storedPassword) {
        if (password == null || storedPassword == null || password.length() > 1024) {
            return false;
        }

        if (!isHashed(storedPassword)) {
            return MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    storedPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            if (iterations < MIN_VERIFY_ITERATIONS || iterations > MAX_VERIFY_ITERATIONS
                    || salt.length != SALT_BYTES || expectedHash.length != KEY_BITS / 8) {
                return false;
            }
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX + "$");
    }

    public static boolean needsRehash(String storedPassword) {
        if (!isHashed(storedPassword)) return true;
        String[] parts = storedPassword.split("\\$", -1);
        try {
            return parts.length != 4 || Integer.parseInt(parts[1]) < ITERATIONS;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    public static boolean isValidNewPassword(String password) {
        return password != null && password.length() >= 12 && password.length() <= 1024;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
                return factory.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
                java.util.Arrays.fill(password, '\0');
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not secure password", ex);
        }
    }
}
