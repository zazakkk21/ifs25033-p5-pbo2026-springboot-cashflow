package org.delcom.ifs25033springbootcashflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotEnvTests {
    @TempDir
    Path tempDir;

    @Test
    void loadsTrimmedValuesAndPreservesSystemAndEnvironmentOverrides() throws IOException {
        String prefix = "CASHFLOW_DOTENV_" + UUID.randomUUID().toString().replace("-", "");
        String plainKey = prefix + "_PLAIN";
        String quotedKey = prefix + "_QUOTED";
        String startQuoteKey = prefix + "_START_QUOTE";
        String endQuoteKey = prefix + "_END_QUOTE";
        String emptyKey = prefix + "_EMPTY";
        String shortKey = prefix + "_SHORT";
        String existingKey = prefix + "_EXISTING";
        String environmentKey = System.getenv().keySet().stream()
                .filter(key -> System.getProperty(key) == null)
                .findFirst()
                .orElseThrow();
        Map<String, String> originalProperties = new HashMap<>();
        for (String key : List.of(plainKey, quotedKey, startQuoteKey, endQuoteKey,
                emptyKey, shortKey, existingKey, environmentKey)) {
            originalProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
        System.setProperty(existingKey, "from-system");

        Path envFile = tempDir.resolve("sample.env");
        Files.write(envFile, List.of(
                "",
                "   # comment",
                "invalid-line",
                "=invalid-key",
                " " + plainKey + " = plain=value  ",
                quotedKey + "=\"hello world\"",
                startQuoteKey + "=\"unfinished",
                endQuoteKey + "=unfinished\"",
                emptyKey + "=\"\"",
                shortKey + "=x",
                existingKey + "=from-file",
                environmentKey + "=from-environment"
        ));

        try {
            DotEnv.load(envFile);

            assertEquals("plain=value", System.getProperty(plainKey));
            assertEquals("hello world", System.getProperty(quotedKey));
            assertEquals("\"unfinished", System.getProperty(startQuoteKey));
            assertEquals("unfinished\"", System.getProperty(endQuoteKey));
            assertEquals("", System.getProperty(emptyKey));
            assertEquals("x", System.getProperty(shortKey));
            assertEquals("from-system", System.getProperty(existingKey));
            assertNull(System.getProperty(environmentKey));

            System.setProperty(environmentKey, "from-system");
            DotEnv.load(envFile);
            assertEquals("from-system", System.getProperty(environmentKey));
        } finally {
            restoreProperties(originalProperties);
        }
    }

    @Test
    void ignoresMissingFilesAndWrapsReadFailures() throws IOException {
        DotEnv.load(tempDir.resolve("missing.env"));

        Path envFile = tempDir.resolve("unreadable.env");
        Files.writeString(envFile, "KEY=value");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> DotEnv.load(envFile, ignored -> {
                    throw new IOException("read failed");
                }));
        assertEquals("Gagal membaca file .env", exception.getMessage());
        assertEquals("read failed", exception.getCause().getMessage());
    }

    private static void restoreProperties(Map<String, String> originalProperties) {
        originalProperties.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }
}
