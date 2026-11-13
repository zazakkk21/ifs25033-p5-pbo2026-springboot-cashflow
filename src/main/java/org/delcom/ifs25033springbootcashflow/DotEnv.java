package org.delcom.ifs25033springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DotEnv {
    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    static void load(Path envFile) {
        load(envFile, Files::readAllLines);
    }

    static void load(Path envFile, LinesReader reader) {
        if (!Files.isRegularFile(envFile)) {
            return;
        }
        try {
            for (String line : reader.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal membaca file .env", exception);
        }
    }

    @FunctionalInterface
    interface LinesReader {
        List<String> readAllLines(Path file) throws IOException;
    }
}
