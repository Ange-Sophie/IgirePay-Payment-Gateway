
package com.igirepay.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class PinUtil {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public static String hashPin(String pin) {
        validatePin(pin);

        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = generateHash(pin, salt, ITERATIONS);

        return "PBKDF2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPin(String pin, String storedValue) {
        if (pin == null || storedValue == null) {
            return false;
        }

        if (!storedValue.startsWith("PBKDF2$")) {
            return pin.equals(storedValue);
        }

        try {
            String[] parts = storedValue.split("\\$");

            if (parts.length != 4) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = generateHash(pin, salt, iterations);

            if (actual.length != expected.length) {
                return false;
            }

            int difference = 0;

            for (int i = 0; i < actual.length; i++) {
                difference |= actual[i] ^ expected[i];
            }

            return difference == 0;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] generateHash(
            String pin, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(
                pin.toCharArray(), salt, iterations, KEY_LENGTH);

        try {
            return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PIN hashing failed.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static void validatePin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException(
                    "PIN must contain exactly four digits.");
        }
    }
}
