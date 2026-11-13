package org.delcom.ifs25033springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String password) {
        return hash(password, () -> MessageDigest.getInstance("SHA-256"));
    }

    static String hash(String password, DigestFactory digestFactory) {
        try {
            byte[] digest = digestFactory.create().digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 tidak tersedia", exception);
        }
    }

    public static boolean matches(String password, String hash) {
        return hash != null && MessageDigest.isEqual(
                hash(password).getBytes(StandardCharsets.US_ASCII),
                hash.getBytes(StandardCharsets.US_ASCII));
    }

    @FunctionalInterface
    interface DigestFactory {
        MessageDigest create() throws NoSuchAlgorithmException;
    }
}
