package org.delcom.ifs25033springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTests {
    @Test
    void hashesPasswordsAndMatchesOnlyTheCorrectHash() {
        String hash = PasswordHasher.hash("secret");

        assertEquals(64, hash.length());
        assertTrue(PasswordHasher.matches("secret", hash));
        assertFalse(PasswordHasher.matches("wrong", hash));
        assertFalse(PasswordHasher.matches("secret", null));
    }

    @Test
    void reportsUnavailableDigestAsIllegalState() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> PasswordHasher.hash("secret", () -> {
                    throw new NoSuchAlgorithmException("SHA-256 unavailable");
                }));

        assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
    }
}
